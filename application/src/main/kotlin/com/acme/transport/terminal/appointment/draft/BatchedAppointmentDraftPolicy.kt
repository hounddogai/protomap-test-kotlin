package com.acme.transport.terminal.appointment.draft

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 77479.0, in order. */
fun batchedAppointmentDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 77479.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedAppointmentDraft(): Int {
    val policy = defaultPolicy("batched-appointment-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a batched appointment draft code in its canonical BAD- form. */
fun normalizeBatchedAppointmentDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BAD-")) {
        return clean
    }
    return "BAD-${clean.replace(" ", "-")}"
}

fun batchedAppointmentDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
