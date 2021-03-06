package net.ostis.common.sctpclient.constants;

public enum SctpCommandType {

    UKNOWN_COMMAND(0x00),
    CHECK_ELEMENT_COMMAND(0x01), // check if specified sc-element
    // exist
    GET_ELEMENT_TYPE_COMMAND(0x02), // return sc-element type
    ERASE_ELEMENT_COMMAND(0x03), // erase specified sc-element
    CREATE_NODE_COMMAND(0x04), // create new sc-node
    CREATE_LINK_COMMAND(0x05), // create new sc-link
    CREATE_ARC_COMMAND(0x06), // create new sc-arc
    GET_ARC_VERTEXES_COMMAND(0x07), // return begin and end element of sc-arc
    GET_LINK_CONTENT_COMMAND(0x09), // return content of sc-link
    FIND_LINKS_COMMAND(0x0a), // return sc-links with specified content
    SET_LINK_CONTENT_COMMAND(0x0b), // setup new content for the link
    ITERATE_ELEMENTS_COMMAND(0x0c), // return base template iteration result
    ITERATE_CONSTRUCTION_COMMAND(0x0d), // return advanced template iteration
    // results
    CREATE_EVENT_COMMAND(0x0e), // create subscription to specified event
    REMOVE_EVENT_COMMAND(0x0f), // destroys specified event subscription
    EMIT_EVENTS_COMMAND(0x10), // emits events to client
    FIND_ELEMENT_BY_SYSIDTF_COMMAND(0xa0), // return sc-element by it system
                                           // identifier
    SET_SYSIDTF_COMMAND(0xa1), // setup new system identifier for sc-element
    STATISTICS_COMMAND(0xa2), // return usage statistics from server
    SHUTDOWN_COMMAND(0xfe); // disconnect client from server;

    private byte value;

    private SctpCommandType(byte value) {

        this.value = value;
    }

    private SctpCommandType(int intValue) {

        this.value = (byte) intValue;
    }

    public byte getValue() {

        return value;
    }

    public static SctpCommandType getByCode(byte code) {

        for (SctpCommandType commandType : values()) {
            if (commandType.getValue() == code) {
                return commandType;
            }
        }
        throw new IllegalArgumentException("Unsupported SctpCommandType");
    }
}
