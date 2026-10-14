package com.acme.transport.network.cutoff.template

import com.acme.transport.kit.retry.defaultPolicy

fun lenientCutoffTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientCutoffTemplate(): Int {
    val policy = defaultPolicy("lenient-cutoff-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a lenient cutoff template code in its canonical LCT- form. */
fun normalizeLenientCutoffTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCT-")) {
        return clean
    }
    return "LCT-${clean.replace(" ", "-")}"
}
