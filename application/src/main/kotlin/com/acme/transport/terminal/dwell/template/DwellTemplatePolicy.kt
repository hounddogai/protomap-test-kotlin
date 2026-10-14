package com.acme.transport.terminal.dwell.template

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a dwell template code in its canonical DT- form. */
fun normalizeDwellTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DT-")) {
        return clean
    }
    return "DT-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDwellTemplate(): Int {
    val policy = defaultPolicy("dwell-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
