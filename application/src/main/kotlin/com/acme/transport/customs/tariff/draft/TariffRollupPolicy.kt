package com.acme.transport.customs.tariff.draft

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a tariff rollup code in its canonical TR- form. */
fun normalizeTariffRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TR-")) {
        return clean
    }
    return "TR-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTariffRollup(): Int {
    val policy = defaultPolicy("tariff-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
