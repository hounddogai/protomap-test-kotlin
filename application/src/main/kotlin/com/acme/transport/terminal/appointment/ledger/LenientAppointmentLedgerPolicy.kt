package com.acme.transport.terminal.appointment.ledger

import com.acme.transport.kit.retry.defaultPolicy

fun lenientAppointmentLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Puts a lenient appointment ledger code in its canonical LAL- form. */
fun normalizeLenientAppointmentLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LAL-")) {
        return clean
    }
    return "LAL-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientAppointmentLedger(): Int {
    val policy = defaultPolicy("lenient-appointment-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 7721.0, in order. */
fun lenientAppointmentLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 7721.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
