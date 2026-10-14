package com.acme.transport.network.node.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling node score calculation. */
interface RollingNodeScoreStep {
    fun describe(): String
    fun adjust(item: RollingNodeScore, value: Double): Double
}

data class MinimumRollingNodeScoreAdjuster(var minimum: Double) : RollingNodeScoreStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingNodeScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRollingNodeScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingNodeScoreStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingNodeScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRollingNodeScoreAdjuster(var cap: Double) : RollingNodeScoreStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingNodeScore, value: Double): Double = minOf(value, cap)
}

data class RoundingRollingNodeScoreAdjuster(var step: Double) : RollingNodeScoreStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingNodeScore, value: Double): Double = roundTo(value, step)
}

data class LocaleRollingNodeScoreAdjuster(var factors: MutableMap<String, Double>) : RollingNodeScoreStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: RollingNodeScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRollingNodeScoreAdjuster(var percent: Double) : RollingNodeScoreStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingNodeScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rolling node score steps in order and keeps a trace of each. */
data class RollingNodeScoreEngine(
    var steps: MutableList<RollingNodeScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingNodeScore): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingNodeScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingNodeScoreEngine(flags: Registry): RollingNodeScoreEngine {
    return RollingNodeScoreEngine(
        steps = mutableListOf<RollingNodeScoreStep>(
            MinimumRollingNodeScoreAdjuster(24.1),
            FlaggedRollingNodeScoreAdjuster(flags, "rolling-node-score-batched", -6.1),
            CapRollingNodeScoreAdjuster(38601.0),
            RoundingRollingNodeScoreAdjuster(0.25),
            LocaleRollingNodeScoreAdjuster(
                mutableMapOf<String, Double>("east" to 1.08, "online" to 0.95, "retail" to 1.0),
            ),
            PercentRollingNodeScoreAdjuster(-13.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 45367.2,
    )
}
