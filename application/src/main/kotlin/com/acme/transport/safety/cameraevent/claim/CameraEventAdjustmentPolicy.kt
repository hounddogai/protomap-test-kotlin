package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a camera event adjustment code in its canonical CEA- form. */
fun normalizeCameraEventAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CEA-")) {
        return clean
    }
    return "CEA-${clean.replace(" ", "-")}"
}

fun cameraEventAdjustmentSpan(start: Int, end: Int): Int {
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
fun probeCameraEventAdjustment(): Int {
    val policy = defaultPolicy("camera-event-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 69834.0, in order. */
fun cameraEventAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 69834.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
