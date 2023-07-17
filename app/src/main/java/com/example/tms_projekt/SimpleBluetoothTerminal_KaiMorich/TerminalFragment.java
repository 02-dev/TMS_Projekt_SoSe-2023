package com.example.tms_projekt.SimpleBluetoothTerminal_KaiMorich;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.tms_projekt.GlobalFunctions;
import com.example.tms_projekt.Protocoll_headers.Data;
import com.example.tms_projekt.Protocoll_headers.RERR;
import com.example.tms_projekt.Protocoll_headers.RREP;
import com.example.tms_projekt.Protocoll_headers.RREQ;
import com.example.tms_projekt.R;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;

@RequiresApi(api = Build.VERSION_CODES.O)
public class TerminalFragment extends Fragment implements ServiceConnection, SerialListener {

    private enum Connected { False, Pending, True }
    private String deviceAddress;
    private SerialService service;
    private TextView receiveText;
    private TextView sendText;
    private TextUtil.HexWatcher hexWatcher;
    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private boolean hexEnabled = false;
    private boolean pendingNewline = false;
    private String newline = TextUtil.newline_crlf;

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        deviceAddress = getArguments().getString("device");
    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if(service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation") // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try { getActivity().unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if(initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);
        receiveText = view.findViewById(R.id.receive_text);                          // TextView performance decreases with number of spans
        receiveText.setTextColor(getResources().getColor(R.color.colorRecieveText)); // set as default color to reduce number of spans
        receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());

        sendText = view.findViewById(R.id.send_text);
        hexWatcher = new TextUtil.HexWatcher(sendText);
        hexWatcher.enable(hexEnabled);
        sendText.addTextChangedListener(hexWatcher);
        sendText.setHint(hexEnabled ? "HEX mode" : "");

        View sendBtn = view.findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(v -> send(sendText.getText().toString()));
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_terminal, menu);
        menu.findItem(R.id.hex).setChecked(hexEnabled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear) {
            receiveText.setText("");
            return true;
        } else if (id == R.id.newline) {
            String[] newlineNames = getResources().getStringArray(R.array.newline_names);
            String[] newlineValues = getResources().getStringArray(R.array.newline_values);
            int pos = java.util.Arrays.asList(newlineValues).indexOf(newline);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Newline");
            builder.setSingleChoiceItems(newlineNames, pos, (dialog, item1) -> {
                newline = newlineValues[item1];
                dialog.dismiss();
            });
            builder.create().show();
            return true;
        } else if (id == R.id.hex) {
            hexEnabled = !hexEnabled;
            sendText.setText("");
            hexWatcher.enable(hexEnabled);
            sendText.setHint(hexEnabled ? "HEX mode" : "");
            item.setChecked(hexEnabled);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Serial + UI
     */
    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    private Boolean notSendingData = true;
    private LocalTime lastHelloMessageSent = LocalTime.now();

    private void send(String str) {
        if(connected != Connected.True) {
            Toast.makeText(getActivity(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (notSendingData && lastHelloMessageSent.plus(15, ChronoUnit.SECONDS).isBefore(LocalTime.now())) {
                try {
                    notSendingData = false;
                    GlobalFunctions.origSeqNum = GlobalFunctions.origSeqNum + 1;
                    RREP rrep = new RREP(GlobalFunctions.origAddr, "FFFF");
                    send("AT+DEST=FFFF");
                    send("AT+SEND=" + rrep.buildRREP().length());
                    send(rrep.buildRREP());
                    lastHelloMessageSent = LocalTime.now();
                    receiveText.append("Hello-Message has been broadcast." + "\n");
                    notSendingData = true;
                } catch (Exception e) {
                    notSendingData = true;
                    receiveText.append("ERROR: Something went wrong when trying to send Hello-Message\n");
                }
            } else if (notSendingData) {
                try {
                    String unreachableNode;
                    for (int i = 0; i < GlobalFunctions.routingTable.size(); i++) {
                        if (GlobalFunctions.getDecimalHopCountOfTargetNodeFromRoutingTable(GlobalFunctions.routingTable.get(i)[0]) == 1 &&LocalTime.parse(GlobalFunctions.routingTable.get(i)[4]).plus(30, ChronoUnit.SECONDS).isBefore(LocalTime.now())) {
                            unreachableNode = GlobalFunctions.routingTable.get(i)[0];
                            GlobalFunctions.deleteTargetFromRoutingTable(unreachableNode);
                            RERR rerr = new RERR(unreachableNode, true);
                            notSendingData = false;
                            send("AT+DEST=FFFF");
                            send("AT+SEND=" + rerr.buildRERR().length());
                            send(rerr.buildRERR());
                            notSendingData = true;
                            receiveText.append("Route to " + unreachableNode + " timed out - Route has been removed from Routing Table" + "\n");
                            for (int j = 0; j < GlobalFunctions.routingTable.size(); j++) {
                                if (GlobalFunctions.routingTable.get(j)[1].equals(unreachableNode)) {
                                    receiveText.append("Route to " + GlobalFunctions.routingTable.get(j)[0] + " has been removed from Routing Table since " + unreachableNode + " has become unreachable" + "\n");
                                    GlobalFunctions.deleteTargetFromRoutingTable(GlobalFunctions.routingTable.get(j)[0]);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    notSendingData = true;
                    receiveText.append("ERROR: Something went wrong when trying to find and remove outdated nodes from Routing Table\n");
                }
            }
            if (str.startsWith("RREQ+") && str.length() == 9) { // format for sending RREQ: RREQ+XXXX  -   XXXX = target address
                String targetAddress = str.substring(5);
                try {
                    Integer.parseInt(targetAddress, 16);
                    GlobalFunctions.origSeqNum = GlobalFunctions.origSeqNum + 1;
                    GlobalFunctions.routRequestID = GlobalFunctions.routRequestID + 1;
                    RREQ rreq = new RREQ(targetAddress, GlobalFunctions.isTargetInRoutingTable(targetAddress));
                    notSendingData = false;
                    send("AT+DEST=FFFF");
                    send("AT+SEND=" + rreq.buildRREQ().length());
                    send(rreq.buildRREQ());
                    notSendingData = true;
                    receiveText.append("RREQ has been broadcast" + "\n");
                } catch (Exception e) {
                    notSendingData = true;
                    receiveText.append("ERROR: Address for RREQ is not in hex format or something went wrong" + "\n");
                }
            } else if (str.startsWith("TO+") && str.length() >= 9) { // format for sending data: TO+XXXX,(data)
                String targetAddress = str.substring(3,7);
                String dataTOBeSent = str.substring(8);
                try {
                    Integer.parseInt(targetAddress, 16);
                    if (GlobalFunctions.isTargetInRoutingTable(targetAddress)) {
                        Data data = new Data(targetAddress, dataTOBeSent);
                        notSendingData = false;
                        send("AT+DEST=" + targetAddress);
                        send("AT+SEND=" + data.buildData().length());
                        send(data.buildData());
                        notSendingData = true;
                        receiveText.append("Data has been sent to " + targetAddress + "\n");
                    } else {
                        receiveText.append("ERROR: Target is not in Routing Table" + "\n");
                    }
                } catch (Exception e) {
                    notSendingData = true;
                    receiveText.append("ERROR: Address for sending Data is not in hex format" + "\n");
                }
            } else {
                String msg;
                byte[] data;
                if(hexEnabled) {
                    StringBuilder sb = new StringBuilder();
                    TextUtil.toHexString(sb, TextUtil.fromHexString(str));
                    TextUtil.toHexString(sb, newline.getBytes());
                    msg = sb.toString();
                    data = TextUtil.fromHexString(msg);
                } else {
                    msg = str;
                    data = (str + newline).getBytes();
                }
                SpannableStringBuilder spn = new SpannableStringBuilder(msg + '\n');
                spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSendText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                receiveText.append(spn);
                service.write(data);
                Thread.sleep(300);
            }
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    private void receive(ArrayDeque<byte[]> datas) {
        SpannableStringBuilder spn = new SpannableStringBuilder();
        for (byte[] data : datas) {
            if (hexEnabled) {
                spn.append(TextUtil.toHexString(data)).append('\n');
            } else {
                String msg = new String(data);
                if (newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
                    // don't show CR as ^M if directly before LF
                    msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf);
                    // special handling if CR and LF come in separate fragments
                    if (pendingNewline && msg.charAt(0) == '\n') {
                        if(spn.length() >= 2) {
                            spn.delete(spn.length() - 2, spn.length());
                        } else {
                            Editable edt = receiveText.getEditableText();
                            if (edt != null && edt.length() >= 2)
                                edt.delete(edt.length() - 2, edt.length());
                        }
                    }
                    pendingNewline = msg.charAt(msg.length() - 1) == '\r';
                }
                spn.append(TextUtil.toCaretString(msg, newline.length() != 0));
            }
        }
        String spnString = spn.toString();
        if (spnString.startsWith("LR") && spnString.length() >= 12) {
            String receivedFromAdr = spnString.substring(3,7);
            String protocolHeader = spnString.substring(11, Integer.parseInt(spnString.substring(8,10), 16) + 11);
            if (protocolHeader.startsWith("0")) {
                try {
                    RREQ rreq = new RREQ(protocolHeader);
                    if (rreq.getOrigSeqNumAsDecimal() > GlobalFunctions.origSeqNum) { GlobalFunctions.origSeqNum = rreq.getOrigSeqNumAsDecimal();}

                    Boolean isOrigAddrInRT = GlobalFunctions.isTargetInRoutingTable(rreq.getOrigAddr());
                    if (!isOrigAddrInRT) {
                        GlobalFunctions.addTargetNodeToRoutingTable(rreq.getOrigAddr(), receivedFromAdr, rreq.getHopCount(), rreq.getOrigSeqNum());
                    } else if (rreq.getHopCountAsDecimal() <= GlobalFunctions.getDecimalHopCountOfTargetNodeFromRoutingTable(rreq.getOrigAddr())) {
                        GlobalFunctions.deleteTargetFromRoutingTable(rreq.getOrigAddr());
                        GlobalFunctions.addTargetNodeToRoutingTable(rreq.getOrigAddr(), receivedFromAdr, rreq.getHopCount(), rreq.getOrigSeqNum());
                    }
                    if (rreq.getDestAddr().equals(GlobalFunctions.origAddr) || GlobalFunctions.isTargetInRoutingTable(rreq.getDestAddr())) {
                        GlobalFunctions.origSeqNum = GlobalFunctions.origSeqNum +1;
                        receiveText.append("Received RREQ from " + rreq.getOrigAddr() + " for self via " + receivedFromAdr + "\n");
                        notSendingData = false;
                        send("AT+DEST=" + GlobalFunctions.getTargetNodeFromRoutingTable(rreq.getOrigAddr())[1]);
                        RREP reply = new RREP(rreq.getDestAddr(), rreq.getOrigAddr());
                        send("AT+SEND=" + reply.buildRREP().length());
                        send(reply.buildRREP());
                        notSendingData = true;
                        receiveText.append("RREP has been dispatched to " + rreq.getOrigAddr() + " via " + GlobalFunctions.getTargetNodeFromRoutingTable(rreq.getOrigAddr())[1] + "\n");
                    } else {
                        // TODO: Nur weiterleiten, wenn Kombi aus RRID und OrigAddr unbekannt.
                        receiveText.append("Received RREQ from " + rreq.getOrigAddr() + " for " + rreq.getDestAddr() + " via" + receivedFromAdr + "\n");
                        notSendingData = false;
                        send("AT+DEST=FFFF");
                        send("AT+SEND=" + rreq.buildRREQ().length());
                        send(rreq.buildRREQ());
                        notSendingData = true;
                        receiveText.append("Received RREQ has been broadcast" + "\n");
                    }
                } catch (Exception e) {
                    notSendingData = true;
                    receiveText.append("ERROR: Something went wrong when processing received RREQ\n");
                }
            } else if (protocolHeader.startsWith("1")) {
                try {
                    RREP rrep = new RREP(protocolHeader);
                    Boolean isDestAddrInRT = GlobalFunctions.isTargetInRoutingTable(rrep.getDestAddr());
                    if (!isDestAddrInRT) {
                        GlobalFunctions.addTargetNodeToRoutingTable(rrep.getDestAddr(), receivedFromAdr, rrep.getHopCount(), rrep.getDestSeqNum());
                    } else if (rrep.getHopCountAsDecimal() <= GlobalFunctions.getDecimalHopCountOfTargetNodeFromRoutingTable(rrep.getDestAddr())) {
                        GlobalFunctions.deleteTargetFromRoutingTable(rrep.getDestAddr());
                        GlobalFunctions.addTargetNodeToRoutingTable(rrep.getDestAddr(), receivedFromAdr, rrep.getHopCount(), rrep.getDestSeqNum());
                    }
                    if (rrep.getDestAddr().equals(GlobalFunctions.origAddr)) {
                        receiveText.append("Received reply to own RREQ from " + rrep.getDestAddr() + " via " + receivedFromAdr + "\n");
                    } else if (!GlobalFunctions.isTargetInRoutingTable(rrep.getOrigAddr())) {
                        receiveText.append("ERROR: Received RREP from " + rrep.getDestAddr() + " for " + rrep.getOrigAddr() + " via " + receivedFromAdr + ", but target is unreachable" + "\n");
                        notSendingData = false;
                        send("AT+DEST=FFFF");
                        RERR rerr = new RERR(rrep.getOrigAddr(), true);
                        send("AT+SEND=" + rerr.buildRERR().length());
                        send(rerr.buildRERR());
                        notSendingData = true;
                        receiveText.append("RERR has been broadcast for node: " + rrep.getOrigAddr() + "\n");
                    } else {
                        receiveText.append("Received RREP from " + rrep.getDestAddr() + " for " + rrep.getOrigAddr() + " via " + receivedFromAdr + "\n");
                        if (rrep.getOrigAddr() != "FFFF") {
                            notSendingData = false;
                            send("AT+DEST=" + GlobalFunctions.getTargetNodeFromRoutingTable(rrep.getOrigAddr())[1]);
                            send("AT+SEND=" + rrep.buildRREP().length());
                            send(rrep.buildRREP());
                            notSendingData = true;
                            receiveText.append("Received RREP has been sent to: " + GlobalFunctions.getTargetNodeFromRoutingTable(rrep.getOrigAddr())[1] + "\n");
                        } else {
                            GlobalFunctions.updateTimeOfTargetNodeFromRT(rrep.getDestAddr());
                        }
                    }
                } catch (Exception e) {
                    notSendingData = true;
                    receiveText.append("ERROR: Something went wrong when processing received RREP\n");
                }
            } else if (protocolHeader.startsWith("2")) {
                try {
                    RERR rerr = new RERR(protocolHeader);
                    if (GlobalFunctions.isTargetInRoutingTable(rerr.getUnreachDestAddr())) {
                        GlobalFunctions.deleteTargetFromRoutingTable(rerr.getUnreachDestAddr());
                        receiveText.append("Received RERR. Route to " + rerr.getUnreachDestAddr() + "has been removed from routing table" + "\n");
                        notSendingData = false;
                        send("AT+DEST=FFFF");
                        send("AT+SEND=" + rerr.buildRERR().length());
                        send(rerr.buildRERR());
                        receiveText.append("RERR has been broadcast for node: " + rerr.getUnreachDestAddr() + "\n");
                        for (int j = 0; j < GlobalFunctions.routingTable.size(); j++) {
                            if (GlobalFunctions.routingTable.get(j)[1].equals(rerr.getUnreachDestAddr())) {
                                receiveText.append("Route to " + GlobalFunctions.routingTable.get(j)[0] + " has been removed from Routing Table since " + rerr.getUnreachDestAddr() + " has become unreachable" + "\n");
                                GlobalFunctions.deleteTargetFromRoutingTable(GlobalFunctions.routingTable.get(j)[0]);
                            }
                        }
                        notSendingData = true;
                    }
                } catch (Exception e) {
                    notSendingData = true;
                    receiveText.append("ERROR: Something went wrong when processing received RERR\n");
                }
            } else if (protocolHeader.startsWith("3")) {
                try {
                    Data data = new Data(protocolHeader);
                    if (data.getDestAddr().equals(GlobalFunctions.origAddr)) {
                        receiveText.append("Message from " + receivedFromAdr + ": " + data.getPayload() + "\n");
                    } else if (!GlobalFunctions.isTargetInRoutingTable(data.getDestAddr())) {
                        receiveText.append("ERROR: Received Data from " + data.getOrigAddr() + " for " + data.getDestAddr() + " via " + receivedFromAdr + ", but target is unreachable" + "\n");
                        notSendingData = false;
                        send("AT+DEST=FFFF");
                        RERR rerr = new RERR(data.getDestAddr(), true);
                        send("AT+SEND=" + rerr.buildRERR().length());
                        send(rerr.buildRERR());
                        notSendingData = true;
                        receiveText.append("RERR has been broadcast for node: " + data.getDestAddr() + "\n");
                    } else {
                        receiveText.append("Received Data from " + data.getOrigAddr() + " for " + data.getDestAddr() + " via" + receivedFromAdr + "\n");
                        notSendingData = false;
                        send("AT+DEST=" + GlobalFunctions.getTargetNodeFromRoutingTable(data.getDestAddr())[1]);
                        send("AT+SEND=" + data.buildData().length());
                        send(data.buildData());
                        notSendingData = true;
                        receiveText.append("Received Data has been sent on to: " + GlobalFunctions.getTargetNodeFromRoutingTable(data.getDestAddr())[1] + "\n");
                    }
                } catch (Exception e) {
                    notSendingData = true;
                    receiveText.append("ERROR: Something went wrong when processing received data\n");
                }
            } else {receiveText.append("ERROR: Received data without valid type." + "\n");}
        } else receiveText.append(spn);
    }

    private void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        receiveText.append(spn);
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
        send("AT+CFG=433920000,5,9,7,4,1,0,0,0,0,3000,8,8");
        send("AT+ADDR=" + GlobalFunctions.origAddr);
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        ArrayDeque<byte[]> datas = new ArrayDeque<>();
        datas.add(data);
        receive(datas);
    }

    public void onSerialRead(ArrayDeque<byte[]> datas) {
        receive(datas);
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }

}
