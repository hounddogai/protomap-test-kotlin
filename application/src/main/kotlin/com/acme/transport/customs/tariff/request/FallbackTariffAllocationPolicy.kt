package com.acme.transport.customs.tariff.request

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a fallback tariff allocation code in its canonical FTA- form. */
fun normalizeFallbackTariffAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FTA-")) {
        return clean
    }
    return "FTA-${clean.replace(" ", "-")}"
}

fun fallbackTariffAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackTariffAllocation(): Int {
    val policy = defaultPolicy("fallback-tariff-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
