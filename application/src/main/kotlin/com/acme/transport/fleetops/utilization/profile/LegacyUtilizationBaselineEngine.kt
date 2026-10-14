package com.acme.transport.fleetops.utilization.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy utilization baseline calculation. */
interface LegacyUtilizationBaselineAdjuster {
    fun describe(): String
    fun adjust(item: LegacyUtilizationBaseline, value: Double): Double
}

data class PercentLegacyUtilizationBaselineAdjuster(var percent: Double) : LegacyUtilizationBaselineAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyUtilizationBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLegacyUtilizationBaselineAdjuster(var step: Double) : LegacyUtilizationBaselineAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyUtilizationBaseline, value: Double): Double = roundTo(value, step)
}

data class LaneLegacyUtilizationBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyUtilizationBaselineAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: LegacyUtilizationBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLegacyUtilizationBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyUtilizationBaselineAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyUtilizationBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LegacyUtilizationBaselineCeilingOffsetAdjuster(
    var weight: Double,
) : LegacyUtilizationBaselineAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: LegacyUtilizationBaseline, value: Double): Double = value + item.ceiling * weight
}

/** Runs the legacy utilization baseline steps in order and keeps a trace of each. */
data class LegacyUtilizationBaselineEngine(
    var steps: MutableList<LegacyUtilizationBaselineAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyUtilizationBaseline): Double {
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

    fun evaluateAll(items: MutableList<LegacyUtilizationBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyUtilizationBaselineEngine(flags: Registry): LegacyUtilizationBaselineEngine {
    return LegacyUtilizationBaselineEngine(
        steps = mutableListOf<LegacyUtilizationBaselineAdjuster>(
            PercentLegacyUtilizationBaselineAdjuster(-5.2),
            RoundingLegacyUtilizationBaselineAdjuster(0.5),
            LaneLegacyUtilizationBaselineAdjuster(mutableMapOf<String, Double>("retail" to 1.06)),
            FlaggedLegacyUtilizationBaselineAdjuster(flags, "legacy-utilization-baseline-adaptive", -4.7),
            LegacyUtilizationBaselineCeilingOffsetAdjuster(1.98),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 32183.3,
    )
}
