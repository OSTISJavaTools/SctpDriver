package net.ostis.common.sctpclient.transport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.ostis.common.sctpclient.constants.ScElementType;
import net.ostis.common.sctpclient.constants.ScIteratorType;
import net.ostis.common.sctpclient.constants.ScParameterSize;
import net.ostis.common.sctpclient.constants.SctpCommandType;
import net.ostis.common.sctpclient.exception.IllegalResultCodeException;
import net.ostis.common.sctpclient.exception.SctpClientException;
import net.ostis.common.sctpclient.exception.TransportException;
import net.ostis.common.sctpclient.model.ScAddress;
import net.ostis.common.sctpclient.model.ScEvent;
import net.ostis.common.sctpclient.model.ScIterator;
import net.ostis.common.sctpclient.model.request.SctpRequest;
import net.ostis.common.sctpclient.model.request.SctpRequestBody;
import net.ostis.common.sctpclient.model.response.SctpResponseHeader;
import net.ostis.common.sctpclient.model.response.SctpResultType;
import net.ostis.common.sctpclient.utils.InputStreamReader;

final class ResponseBodyBuilderProvider {

    private interface SuccessAction<T> {

        T doAction() throws SctpClientException;
    }

    private class SctpResultTypeHandler {

        <T> T handle(SctpResponseHeader responseHeader, SuccessAction<T> success)
                throws SctpClientException {

            SctpResultType resultType = responseHeader.getResultType();
            switch (resultType) {
                case SCTP_RESULT_OK:
                    return success.doAction();
                    // TDOD Change exception type.
                case SCTP_RESULT_FAIL:
                    throw new ElementNotFoundException("");
                case SCTP_RESULT_ERROR_NO_ELEMENT:
                    throw new SctpClientException("");
                default:
                    throw new IllegalResultCodeException("");
            }
        }
    }

    private class AddressWhenSuccessBuilder implements RespBodyBuilder<ScAddress> {

        @Override
        public ScAddress getAnswer(final byte[] bytes, final SctpResponseHeader responseHeader)
                throws SctpClientException {

            return new SctpResultTypeHandler().handle(responseHeader,
                    new SuccessAction<ScAddress>() {

                        @Override
                        public ScAddress doAction() {

                            return TypeBuilder.buildScAddress(bytes);
                        }
                    });
        }
    }

    private class ElementTypeBuilder implements RespBodyBuilder<ScElementType> {

        @Override
        public ScElementType getAnswer(final byte[] bytes, final SctpResponseHeader responseHeader)
                throws SctpClientException {

            return new SctpResultTypeHandler().handle(responseHeader,
                    new SuccessAction<ScElementType>() {

                        @Override
                        public ScElementType doAction() throws SctpClientException {

                            final byte[] elementCode = Arrays.copyOf(bytes,
                                    ScElementType.SC_ELEMENT_TYPE_BYTE_SIZE);
                            final short code = ByteBuffer.wrap(elementCode)
                                    .order(ByteOrder.LITTLE_ENDIAN).getShort();
                            for (final ScElementType elementType : ScElementType.values()) {
                                if (code == elementType.getValue()) {
                                    return elementType;
                                }
                            }
                            throw new SctpClientException("");
                        }
                    });
        }

    }

    private class EmptyResponseBodyBuider implements RespBodyBuilder<Boolean> {

        @Override
        public Boolean getAnswer(final byte[] bytes, final SctpResponseHeader responseHeader) {

            final SctpResultType resultType = responseHeader.getResultType();
            return SctpResultType.SCTP_RESULT_OK == resultType;
        }

    }

    private class FindLinksBuilder implements RespBodyBuilder<List<ScAddress>> {

        private static final int LINKS_NUMBER_END_INDEX = 4;

        private static final int LINKS_NUMBER_BEGIN_INDEX = 0;

        private static final int LINKS_ADDRESSES_BEGIN_INDEX = 4;

        @Override
        public List<ScAddress> getAnswer(final byte[] bytes, final SctpResponseHeader responseHeader)
                throws SctpClientException {

            return new SctpResultTypeHandler().handle(responseHeader,
                    new SuccessAction<List<ScAddress>>() {

                        @Override
                        public List<ScAddress> doAction() {

                            final List<ScAddress> list = new ArrayList<ScAddress>();
                            final int linksNumber = getLinkNumbers(bytes);
                            final Collection<ScAddress> addresses = getAddresses(bytes, linksNumber);
                            list.addAll(addresses);
                            return list;
                        }
                    });
        }

        private int getLinkNumbers(final byte[] bytes) {

            final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, LINKS_NUMBER_BEGIN_INDEX,
                    LINKS_NUMBER_END_INDEX);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            return byteBuffer.getInt();
        }

        private Collection<ScAddress> getAddresses(final byte[] bytes, final int number) {

            final List<ScAddress> list = new LinkedList<ScAddress>();
            int beginIndex = LINKS_ADDRESSES_BEGIN_INDEX;
            for (int i = 0; i < number; i++) {
                byte[] subArray = Arrays.copyOfRange(bytes, beginIndex, beginIndex
                        + ScParameterSize.SC_ADDRESS.getSize());
                final ScAddress address = TypeBuilder.buildScAddress(subArray);
                list.add(address);
                beginIndex += ScParameterSize.SC_ADDRESS.getSize();
            }
            return list;
        }

    }

    private class GetArcVertexesBuilder implements RespBodyBuilder<List<ScAddress>> {

        private static final int END_ADDRESS_BEGIN_INDEX = 4;

        @Override
        public List<ScAddress> getAnswer(final byte[] bytes, final SctpResponseHeader responseHeader)
                throws SctpClientException {

            return new SctpResultTypeHandler().handle(responseHeader,
                    new SuccessAction<List<ScAddress>>() {

                        @Override
                        public List<ScAddress> doAction() {

                            final List<ScAddress> list = new ArrayList<ScAddress>();
                            final ScAddress begin = TypeBuilder.buildScAddress(bytes);
                            byte[] subArray = Arrays.copyOfRange(bytes, END_ADDRESS_BEGIN_INDEX,
                                    END_ADDRESS_BEGIN_INDEX + ScParameterSize.SC_ADDRESS.getSize());
                            final ScAddress end = TypeBuilder.buildScAddress(subArray);
                            list.add(begin);
                            list.add(end);
                            return list;
                        }
                    });

        }
    }

    class IdWhenSuccessBuilder implements RespBodyBuilder<Integer> {

        @Override
        public Integer getAnswer(final byte[] bytes, final SctpResponseHeader responseHeader)
                throws SctpClientException {

            return new SctpResultTypeHandler().handle(responseHeader, new SuccessAction<Integer>() {

                @Override
                public Integer doAction() {

                    final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    return byteBuffer.getInt();
                }
            });

        }

    }

    class LinkContentBuilder implements RespBodyBuilder<String> {

        @Override
        public String getAnswer(final byte[] bytes, final SctpResponseHeader responseHeader)
                throws SctpClientException {

            return new SctpResultTypeHandler().handle(responseHeader, new SuccessAction<String>() {

                @Override
                public String doAction() {

                    return new String(bytes);
                }
            });
        }

    }

    class SctpIteratorResponseBuilder implements RespBodyBuilder<List<ScIterator>> {

        private SctpRequest sctpRequest;

        private static final int ITERATOR_TYPE_PARAMETER_INDEX = 0;

        public SctpIteratorResponseBuilder(SctpRequest sctpRequest) {

            this.sctpRequest = sctpRequest;
        }

        @Override
        public List<ScIterator> getAnswer(byte[] bytes, SctpResponseHeader responseHeader)
                throws SctpClientException {

            final ScIteratorType iteratorType = getIteratorTypeFromRequest();
            final int parameterNumber = ScIteratorParameterNumberResolver
                    .getParameterNumberByIteratorType(iteratorType);
            final ByteArrayInputStream source = new ByteArrayInputStream(bytes);
            try {
                final int answerIterCount = InputStreamReader.readInt(source);

                return new SctpResultTypeHandler().handle(responseHeader,
                        new SuccessAction<List<ScIterator>>() {

                            @Override
                            public List<ScIterator> doAction() throws SctpClientException {

                                List<ScIterator> result = new ArrayList<ScIterator>();
                                for (int iterIndex = 0; iterIndex < answerIterCount; ++iterIndex) {
                                    ScIterator scIterator = new ScIterator();
                                    for (int j = 0; j < parameterNumber; ++j) {
                                        try {
                                            ScAddress scAddress = TypeBuilder
                                                    .buildScAddress(source);
                                            scIterator.registerParameter(scAddress);
                                        } catch (IOException e) {
                                            throw new SctpClientException(e);
                                        }

                                    }
                                    result.add(scIterator);
                                }
                                return result;
                            }
                        });
            } catch (IOException e) {
                throw new TransportException(e);
            }
        }

        private ScIteratorType getIteratorTypeFromRequest() {

            SctpRequestBody requestBody = sctpRequest.getBody();
            return (ScIteratorType) requestBody.getParameterList().get(
                    ITERATOR_TYPE_PARAMETER_INDEX);
        }
    }

    class ScEventListBuilder implements RespBodyBuilder<List<ScEvent>> {

        public ScEventListBuilder() {

        }

        @Override
        public List<ScEvent> getAnswer(byte[] bytes, SctpResponseHeader responseHeader)
                throws SctpClientException {
            final ByteArrayInputStream source = new ByteArrayInputStream(bytes);
            try {
                final int scEventsNumber = InputStreamReader.readInt(source);
                return new SctpResultTypeHandler().handle(responseHeader,
                        new SuccessAction<List<ScEvent>>() {

                            @Override
                            public List<ScEvent> doAction() throws SctpClientException {
                                List<ScEvent> result = new ArrayList<ScEvent>();
                                for (int index = 0; index < scEventsNumber; ++index) {
                                    ScEvent scEvent = new ScEvent();
                                    try {
										scEvent.setSubscriptionId(InputStreamReader.readInt(source));
										scEvent.setElement(TypeBuilder.buildScAddress(source));
	                                    scEvent.setArgument(TypeBuilder.buildScAddress(source));
									} catch (IOException e) {
										throw new SctpClientException(e);
									}
                                    result.add(scEvent);
                                }
                                return result;
                            }
                        });
            } catch (IOException e) {
                throw new TransportException(e);
            }
        }
    }

    @SuppressWarnings("rawtypes")
	public RespBodyBuilder create(final SctpCommandType command, SctpRequest sctpRequest) {

        switch (command) {
            case CHECK_ELEMENT_COMMAND:
                return new EmptyResponseBodyBuider();
            case GET_ELEMENT_TYPE_COMMAND:
                return new ElementTypeBuilder();
            case ERASE_ELEMENT_COMMAND:
                return new EmptyResponseBodyBuider();
            case CREATE_NODE_COMMAND:
                return new AddressWhenSuccessBuilder();
            case CREATE_LINK_COMMAND:
                return new AddressWhenSuccessBuilder();
            case CREATE_ARC_COMMAND:
                return new AddressWhenSuccessBuilder();
            case GET_ARC_VERTEXES_COMMAND:
                return new GetArcVertexesBuilder();
            case GET_LINK_CONTENT_COMMAND:
                return new LinkContentBuilder();
            case FIND_LINKS_COMMAND:
                return new FindLinksBuilder();
            case SET_LINK_CONTENT_COMMAND:
                return new EmptyResponseBodyBuider();
            case CREATE_EVENT_COMMAND:
                return new IdWhenSuccessBuilder();
            case REMOVE_EVENT_COMMAND:
                return new IdWhenSuccessBuilder();
            case EMIT_EVENTS_COMMAND:
            	return new ScEventListBuilder();
            case FIND_ELEMENT_BY_SYSIDTF_COMMAND:
                return new AddressWhenSuccessBuilder();
            case SET_SYSIDTF_COMMAND:
                return new EmptyResponseBodyBuider();
            case ITERATE_ELEMENTS_COMMAND:
                return new SctpIteratorResponseBuilder(sctpRequest);
            default:
                // TODO: 0x02 Ask ElementTypes and add to ScElementType enum
                // TODO: 0x08 - undefined on sc-machine wiki
                // TODO: 0x0d - Complex construction iteration
                // TODO: 0x10 - Passed event inquiry
                // TODO:0xa2 - Server statistics in time interval
                // TODO:0xa3 - Ask protocol version(ask encoding)
                throw new IllegalArgumentException("Not support command= " + command);
        }
    }

}
