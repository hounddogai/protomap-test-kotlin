package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictZoneSchedule(): Int {
    val policy = defaultPolicy("strict-zone-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeStrictZoneScheduleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 75244.0, in order. */
fun strictZoneScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 75244.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a strict zone schedule code in its canonical SZS- form. */
fun normalizeStrictZoneScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SZS-")) {
        return clean
    }
    return "SZS-${clean.replace(" ", "-")}"
}
