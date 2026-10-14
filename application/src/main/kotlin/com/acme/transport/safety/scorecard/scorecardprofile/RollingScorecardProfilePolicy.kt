package com.acme.transport.safety.scorecard.scorecardprofile

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 83992.0, in order. */
fun rollingScorecardProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 83992.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeRollingScorecardProfileLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingScorecardProfile(): Int {
    val policy = defaultPolicy("rolling-scorecard-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a rolling scorecard profile score falls in. */
fun classifyRollingScorecardProfile(score: Double): String {
    if (score < 22.2) {
        return "minimal"
    }
    if (score < 71.6) {
        return "low"
    }
    return "watch"
}
