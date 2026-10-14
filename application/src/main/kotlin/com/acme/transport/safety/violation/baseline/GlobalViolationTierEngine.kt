package com.acme.transport.safety.violation.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global violation tier calculation. */
interface GlobalViolationTierFactor {
    fun describe(): String
    fun adjust(item: GlobalViolationTier, value: Double): Double
}

data class PercentGlobalViolationTierAdjuster(var percent: Double) : GlobalViolationTierFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalViolationTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneGlobalViolationTierAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalViolationTierFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: GlobalViolationTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedGlobalViolationTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalViolationTierFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalViolationTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingGlobalViolationTierAdjuster(var step: Double) : GlobalViolationTierFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalViolationTier, value: Double): Double = roundTo(value, step)
}

/** Runs the global violation tier steps in order and keeps a trace of each. */
data class GlobalViolationTierEngine(
    var steps: MutableList<GlobalViolationTierFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalViolationTier): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalViolationTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalViolationTierEngine(flags: Registry): GlobalViolationTierEngine {
    return GlobalViolationTierEngine(
        steps = mutableListOf<GlobalViolationTierFactor>(
            PercentGlobalViolationTierAdjuster(19.1),
            LaneGlobalViolationTierAdjuster(mutableMapOf<String, Double>("north" to 0.84)),
            FlaggedGlobalViolationTierAdjuster(flags, "global-violation-tier-scheduled", 3.8),
            RoundingGlobalViolationTierAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 21003.7,
    )
}
