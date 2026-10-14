package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyDisputeTemplate(): Int {
    val policy = defaultPolicy("legacy-dispute-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a legacy dispute template code in its canonical LDT- form. */
fun normalizeLegacyDisputeTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LDT-")) {
        return clean
    }
    return "LDT-${clean.replace(" ", "-")}"
}
