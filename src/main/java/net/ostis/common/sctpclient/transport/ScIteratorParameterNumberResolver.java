package net.ostis.common.sctpclient.transport;

import java.util.EnumMap;
import java.util.Map;

import net.ostis.common.sctpclient.constants.ScIteratorType;

public class ScIteratorParameterNumberResolver {

    private static Map<ScIteratorType, Integer> numberScIteratorType;

    static {
        numberScIteratorType = new EnumMap<>(ScIteratorType.class);
        numberScIteratorType.put(ScIteratorType.SCTP_ITERATOR_3_A_A_F, 3);
        numberScIteratorType.put(ScIteratorType.SCTP_ITERATOR_3_F_A_A, 3);
        numberScIteratorType.put(ScIteratorType.SCTP_ITERATOR_3_F_A_F, 3);
        numberScIteratorType.put(ScIteratorType.SCTP_ITERATOR_5_A_A_F_A_A, 5);
        numberScIteratorType.put(ScIteratorType.SCTP_ITERATOR_5_A_A_F_A_F, 5);
        numberScIteratorType.put(ScIteratorType.SCTP_ITERATOR_5_F_A_A_A_A, 5);
        numberScIteratorType.put(ScIteratorType.SCTP_ITERATOR_5_F_A_A_A_F, 5);
        numberScIteratorType.put(ScIteratorType.SCTP_ITERATOR_5_F_A_F_A_A, 5);
        numberScIteratorType.put(ScIteratorType.SCTP_ITERATOR_5_F_A_F_A_F, 5);
    }

    public static Integer getParameterNumberByIteratorType(ScIteratorType iteratorType) {

        return numberScIteratorType.get(iteratorType);
    }

}
