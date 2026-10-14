package com.acme.transport.network.cutoff.batch

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a strict cutoff rollup score falls in. */
fun classifyStrictCutoffRollup(score: Double): String {
    if (score < 47.4) {
        return "minimal"
    }
    if (score < 84.4) {
        return "ok"
    }
    return "medium"
}

fun strictCutoffRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.35
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
