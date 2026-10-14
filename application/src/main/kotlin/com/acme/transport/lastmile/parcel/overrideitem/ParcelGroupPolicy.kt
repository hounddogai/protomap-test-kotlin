package com.acme.transport.lastmile.parcel.overrideitem

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeParcelGroup(): Int {
    val policy = defaultPolicy("parcel-group")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun parcelGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.05
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 7154.0, in order. */
fun parcelGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 7154.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a parcel group score falls in. */
fun classifyParcelGroup(score: Double): String {
    if (score < 66.3) {
        return "medium"
    }
    if (score < 67.6) {
        return "critical"
    }
    return "low"
}
