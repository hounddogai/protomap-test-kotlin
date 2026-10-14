package com.acme.transport.crewing.qualification.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 32130.0, in order. */
fun qualificationReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 32130.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a qualification reservation score falls in. */
fun classifyQualificationReservation(score: Double): String {
    if (score < 20.8) {
        return "minimal"
    }
    if (score < 52.8) {
        return "watch"
    }
    if (score < 81.2) {
        return "low"
    }
    return "ok"
}

fun mergeQualificationReservationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
