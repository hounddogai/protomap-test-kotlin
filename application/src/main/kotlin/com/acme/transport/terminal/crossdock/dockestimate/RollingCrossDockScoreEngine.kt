package com.acme.transport.terminal.crossdock.dockestimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling cross dock score calculation. */
interface RollingCrossDockScoreStep {
    fun describe(): String
    fun adjust(item: RollingCrossDockScore, value: Double): Double
}

data class RoundingRollingCrossDockScoreAdjuster(var step: Double) : RollingCrossDockScoreStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingCrossDockScore, value: Double): Double = roundTo(value, step)
}

data class TitleRollingCrossDockScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingCrossDockScoreStep {
    override fun describe(): String = "by title"

    override fun adjust(item: RollingCrossDockScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRollingCrossDockScoreAdjuster(var cap: Double) : RollingCrossDockScoreStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingCrossDockScore, value: Double): Double = minOf(value, cap)
}

data class FlaggedRollingCrossDockScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingCrossDockScoreStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingCrossDockScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RollingCrossDockScoreScoreOffsetAdjuster(var weight: Double) : RollingCrossDockScoreStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: RollingCrossDockScore, value: Double): Double = value + item.score * weight
}

/** Runs the rolling cross dock score steps in order and keeps a trace of each. */
data class RollingCrossDockScoreEngine(
    var steps: MutableList<RollingCrossDockScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingCrossDockScore): Double {
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

    fun evaluateAll(items: MutableList<RollingCrossDockScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingCrossDockScoreEngine(flags: Registry): RollingCrossDockScoreEngine {
    return RollingCrossDockScoreEngine(
        steps = mutableListOf<RollingCrossDockScoreStep>(
            RoundingRollingCrossDockScoreAdjuster(0.25),
            TitleRollingCrossDockScoreAdjuster(
                mutableMapOf<String, Double>("north" to 1.14, "online" to 1.14, "east" to 1.01),
            ),
            CapRollingCrossDockScoreAdjuster(3665.0),
            FlaggedRollingCrossDockScoreAdjuster(flags, "rolling-cross-dock-score-capped", -5.8),
            RollingCrossDockScoreScoreOffsetAdjuster(0.82),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 59350.1,
    )
}
