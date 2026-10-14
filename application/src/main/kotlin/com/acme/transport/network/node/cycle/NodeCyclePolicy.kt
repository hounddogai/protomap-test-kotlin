package com.acme.transport.network.node.cycle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a node cycle score falls in. */
fun classifyNodeCycle(score: Double): String {
    if (score < 33.5) {
        return "minimal"
    }
    if (score < 61.3) {
        return "high"
    }
    return "low"
}

fun nodeCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.66
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
