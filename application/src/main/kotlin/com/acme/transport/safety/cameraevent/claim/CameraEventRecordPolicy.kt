package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 47194.0, in order. */
fun cameraEventRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 47194.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a camera event record code in its canonical CER- form. */
fun normalizeCameraEventRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CER-")) {
        return clean
    }
    return "CER-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCameraEventRecord(): Int {
    val policy = defaultPolicy("camera-event-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
