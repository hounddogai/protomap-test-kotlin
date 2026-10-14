package com.acme.transport.crewing.dutyperiod.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy duty period mapping calculation. */
interface LegacyDutyPeriodMappingModifier {
    fun describe(): String
    fun adjust(item: LegacyDutyPeriodMapping, value: Double): Double
}

data class PercentLegacyDutyPeriodMappingAdjuster(var percent: Double) : LegacyDutyPeriodMappingModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyDutyPeriodMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLegacyDutyPeriodMappingAdjuster(var cap: Double) : LegacyDutyPeriodMappingModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyDutyPeriodMapping, value: Double): Double = minOf(value, cap)
}

data class TieredLegacyDutyPeriodMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyDutyPeriodMappingModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyDutyPeriodMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedLegacyDutyPeriodMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyDutyPeriodMappingModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyDutyPeriodMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneLegacyDutyPeriodMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyDutyPeriodMappingModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: LegacyDutyPeriodMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LegacyDutyPeriodMappingPriorityOffsetAdjuster(var weight: Double) : LegacyDutyPeriodMappingModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: LegacyDutyPeriodMapping, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

/** Runs the legacy duty period mapping steps in order and keeps a trace of each. */
data class LegacyDutyPeriodMappingEngine(
    var steps: MutableList<LegacyDutyPeriodMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyDutyPeriodMapping): Double {
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

    fun evaluateAll(items: MutableList<LegacyDutyPeriodMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyDutyPeriodMappingEngine(flags: Registry): LegacyDutyPeriodMappingEngine {
    return LegacyDutyPeriodMappingEngine(
        steps = mutableListOf<LegacyDutyPeriodMappingModifier>(
            PercentLegacyDutyPeriodMappingAdjuster(9.9),
            CapLegacyDutyPeriodMappingAdjuster(26793.0),
            TieredLegacyDutyPeriodMappingAdjuster(
                mutableListOf<Double>(4667.0, 4971.0, 7393.0),
                mutableListOf<Double>(1.15, 1.19, 1.1),
            ),
            FlaggedLegacyDutyPeriodMappingAdjuster(flags, "legacy-duty-period-mapping-legacy", -9.5),
            LaneLegacyDutyPeriodMappingAdjuster(mutableMapOf<String, Double>("central" to 0.78)),
            LegacyDutyPeriodMappingPriorityOffsetAdjuster(0.06),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 53186.6,
    )
}
