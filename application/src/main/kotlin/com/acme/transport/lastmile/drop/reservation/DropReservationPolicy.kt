package com.acme.transport.lastmile.drop.reservation

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a drop reservation code in its canonical DR- form. */
fun normalizeDropReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DR-")) {
        return clean
    }
    return "DR-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDropReservation(): Int {
    val policy = defaultPolicy("drop-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 44553.0, in order. */
fun dropReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 44553.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
