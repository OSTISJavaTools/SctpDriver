/**
 * 
 */
package net.ostis.common.sctpclient.exception;

/**
 * @author Andrew Nepogoda Mar 21, 2015
 */
@SuppressWarnings("serial")
public class SctpClientException extends Exception {

    /**
     * @param e
     */
    public SctpClientException(Exception e) {

        super(e);
    }

    /**
     * @param message
     */
    public SctpClientException(String message) {

        super(message);
    }

}
