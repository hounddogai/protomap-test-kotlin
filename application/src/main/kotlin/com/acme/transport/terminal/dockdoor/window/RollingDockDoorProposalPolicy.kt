package com.acme.transport.terminal.dockdoor.window

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingDockDoorProposal(): Int {
    val policy = defaultPolicy("rolling-dock-door-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun rollingDockDoorProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a rolling dock door proposal score falls in. */
fun classifyRollingDockDoorProposal(score: Double): String {
    if (score < 1.4) {
        return "elevated"
    }
    if (score < 65.4) {
        return "minimal"
    }
    return "watch"
}
