package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a batched tariff rollup code in its canonical BTR- form. */
fun normalizeBatchedTariffRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BTR-")) {
        return clean
    }
    return "BTR-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedTariffRollup(): Int {
    val policy = defaultPolicy("batched-tariff-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
