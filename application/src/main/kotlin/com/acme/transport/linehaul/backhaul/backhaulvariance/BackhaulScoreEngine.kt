package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the backhaul score calculation. */
interface BackhaulScoreModifier {
    fun describe(): String
    fun adjust(item: BackhaulScore, value: Double): Double
}

data class ChannelBackhaulScoreAdjuster(var factors: MutableMap<String, Double>) : BackhaulScoreModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: BackhaulScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedBackhaulScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BackhaulScoreModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BackhaulScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredBackhaulScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BackhaulScoreModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BackhaulScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the backhaul score steps in order and keeps a trace of each. */
data class BackhaulScoreEngine(
    var steps: MutableList<BackhaulScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BackhaulScore): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BackhaulScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBackhaulScoreEngine(flags: Registry): BackhaulScoreEngine {
    return BackhaulScoreEngine(
        steps = mutableListOf<BackhaulScoreModifier>(
            ChannelBackhaulScoreAdjuster(mutableMapOf<String, Double>("retail" to 0.97)),
            FlaggedBackhaulScoreAdjuster(flags, "backhaul-score-capped", -2.2),
            TieredBackhaulScoreAdjuster(
                mutableListOf<Double>(2962.0, 6094.0),
                mutableListOf<Double>(0.89, 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 99840.8,
    )
}
