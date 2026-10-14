package com.acme.transport.freightpay.accessorial.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global accessorial score calculation. */
interface GlobalAccessorialScoreAdjuster {
    fun describe(): String
    fun adjust(item: GlobalAccessorialScore, value: Double): Double
}

data class FlaggedGlobalAccessorialScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalAccessorialScoreAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalAccessorialScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingGlobalAccessorialScoreAdjuster(var step: Double) : GlobalAccessorialScoreAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalAccessorialScore, value: Double): Double = roundTo(value, step)
}

data class CapGlobalAccessorialScoreAdjuster(var cap: Double) : GlobalAccessorialScoreAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalAccessorialScore, value: Double): Double = minOf(value, cap)
}

data class UnitGlobalAccessorialScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalAccessorialScoreAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: GlobalAccessorialScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentGlobalAccessorialScoreAdjuster(var percent: Double) : GlobalAccessorialScoreAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalAccessorialScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the global accessorial score steps in order and keeps a trace of each. */
data class GlobalAccessorialScoreEngine(
    var steps: MutableList<GlobalAccessorialScoreAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalAccessorialScore): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalAccessorialScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalAccessorialScoreEngine(flags: Registry): GlobalAccessorialScoreEngine {
    return GlobalAccessorialScoreEngine(
        steps = mutableListOf<GlobalAccessorialScoreAdjuster>(
            FlaggedGlobalAccessorialScoreAdjuster(flags, "global-accessorial-score-lenient", -9.0),
            RoundingGlobalAccessorialScoreAdjuster(0.25),
            CapGlobalAccessorialScoreAdjuster(39406.0),
            UnitGlobalAccessorialScoreAdjuster(mutableMapOf<String, Double>("online" to 0.7, "north" to 0.97)),
            PercentGlobalAccessorialScoreAdjuster(17.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 83181.3,
    )
}
