package com.acme.transport.customs.tariff.quota

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTariffVariance(): Int {
    val policy = defaultPolicy("tariff-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a tariff variance code in its canonical TV- form. */
fun normalizeTariffVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TV-")) {
        return clean
    }
    return "TV-${clean.replace(" ", "-")}"
}
