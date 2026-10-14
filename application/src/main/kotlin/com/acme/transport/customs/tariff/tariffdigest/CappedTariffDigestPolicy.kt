package com.acme.transport.customs.tariff.tariffdigest

import com.acme.transport.kit.retry.defaultPolicy

fun cappedTariffDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedTariffDigest(): Int {
    val policy = defaultPolicy("capped-tariff-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
