package com.acme.transport.crewing.dutyperiod.mapping

import com.acme.transport.kit.flags.Registry

/** One step of the duty period mapping calculation. */
interface DutyPeriodMappingStep {
    fun describe(): String
    fun adjust(item: DutyPeriodMapping, value: Double): Double
}

data class FlaggedDutyPeriodMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DutyPeriodMappingStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DutyPeriodMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDutyPeriodMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DutyPeriodMappingStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DutyPeriodMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameDutyPeriodMappingAdjuster(var factors: MutableMap<String, Double>) : DutyPeriodMappingStep {
    override fun describe(): String = "by name"

    override fun adjust(item: DutyPeriodMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDutyPeriodMappingAdjuster(var percent: Double) : DutyPeriodMappingStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DutyPeriodMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DutyPeriodMappingEtaMinutesOffsetAdjuster(var weight: Double) : DutyPeriodMappingStep {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: DutyPeriodMapping, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class MinimumDutyPeriodMappingAdjuster(var minimum: Double) : DutyPeriodMappingStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DutyPeriodMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the duty period mapping steps in order and keeps a trace of each. */
data class DutyPeriodMappingEngine(
    var steps: MutableList<DutyPeriodMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DutyPeriodMapping): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DutyPeriodMappingStep = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DutyPeriodMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDutyPeriodMappingEngine(flags: Registry): DutyPeriodMappingEngine {
    return DutyPeriodMappingEngine(
        steps = mutableListOf<DutyPeriodMappingStep>(
            FlaggedDutyPeriodMappingAdjuster(flags, "duty-period-mapping-weighted", -8.1),
            TieredDutyPeriodMappingAdjuster(
                mutableListOf<Double>(2681.0, 3836.0, 4126.0, 5555.0),
                mutableListOf<Double>(0.94, 0.82, 0.96, 0.88),
            ),
            NameDutyPeriodMappingAdjuster(
                mutableMapOf<String, Double>("online" to 1.33, "east" to 1.33, "central" to 1.09),
            ),
            PercentDutyPeriodMappingAdjuster(-21.2),
            DutyPeriodMappingEtaMinutesOffsetAdjuster(0.11),
            MinimumDutyPeriodMappingAdjuster(32.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 81237.2,
    )
}
