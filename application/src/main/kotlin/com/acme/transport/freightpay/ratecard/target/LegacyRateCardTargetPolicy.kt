package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a legacy rate card target code in its canonical LRCT- form. */
fun normalizeLegacyRateCardTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRCT-")) {
        return clean
    }
    return "LRCT-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyRateCardTarget(): Int {
    val policy = defaultPolicy("legacy-rate-card-target")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
