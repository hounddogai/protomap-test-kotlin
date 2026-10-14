package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Puts a batched accessorial reservation code in its canonical BAR- form. */
fun normalizeBatchedAccessorialReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BAR-")) {
        return clean
    }
    return "BAR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 2796.0, in order. */
fun batchedAccessorialReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2796.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedAccessorialReservation(): Int {
    val policy = defaultPolicy("batched-accessorial-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeBatchedAccessorialReservationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
