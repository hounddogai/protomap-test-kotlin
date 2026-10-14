package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped utilization preference calculation. */
interface CappedUtilizationPreferenceModifier {
    fun describe(): String
    fun adjust(item: CappedUtilizationPreference, value: Double): Double
}

data class FlaggedCappedUtilizationPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedUtilizationPreferenceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedUtilizationPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumCappedUtilizationPreferenceAdjuster(
    var minimum: Double,
) : CappedUtilizationPreferenceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedUtilizationPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CountryCappedUtilizationPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedUtilizationPreferenceModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: CappedUtilizationPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentCappedUtilizationPreferenceAdjuster(
    var percent: Double,
) : CappedUtilizationPreferenceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedUtilizationPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped utilization preference steps in order and keeps a trace of each. */
data class CappedUtilizationPreferenceEngine(
    var steps: MutableList<CappedUtilizationPreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedUtilizationPreference): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedUtilizationPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedUtilizationPreferenceEngine(flags: Registry): CappedUtilizationPreferenceEngine {
    return CappedUtilizationPreferenceEngine(
        steps = mutableListOf<CappedUtilizationPreferenceModifier>(
            FlaggedCappedUtilizationPreferenceAdjuster(flags, "capped-utilization-preference-lenient", -9.4),
            MinimumCappedUtilizationPreferenceAdjuster(13.9),
            CountryCappedUtilizationPreferenceAdjuster(
                mutableMapOf<String, Double>("north" to 0.94, "east" to 1.05),
            ),
            PercentCappedUtilizationPreferenceAdjuster(18.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 37932.1,
    )
}
