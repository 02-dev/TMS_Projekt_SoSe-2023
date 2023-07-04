package Protocoll_headers;

public enum MessageType {
    RREQ_t("0"),
    RREP_t("1"),
    RERR_t("2"),
    DATA_t("3"),
    DATA_ACK("4");

    private String type;

    MessageType(String typeString) {
        this.type = typeString;
    }

    public String getType() {
        return type;
    }
}
