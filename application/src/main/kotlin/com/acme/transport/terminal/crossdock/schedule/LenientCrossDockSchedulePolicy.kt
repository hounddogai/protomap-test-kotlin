package com.acme.transport.terminal.crossdock.schedule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a lenient cross dock schedule score falls in. */
fun classifyLenientCrossDockSchedule(score: Double): String {
    if (score < 67.8) {
        return "high"
    }
    if (score < 97.3) {
        return "low"
    }
    return "minimal"
}

fun mergeLenientCrossDockScheduleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 59894.0, in order. */
fun lenientCrossDockScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 59894.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientCrossDockSchedule(): Int {
    val policy = defaultPolicy("lenient-cross-dock-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
