package com.acme.transport.linehaul.trailer.bundle

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a lenient trailer bundle code in its canonical LTB- form. */
fun normalizeLenientTrailerBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LTB-")) {
        return clean
    }
    return "LTB-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientTrailerBundle(): Int {
    val policy = defaultPolicy("lenient-trailer-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 74697.0, in order. */
fun lenientTrailerBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74697.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun lenientTrailerBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
