package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending duty period template calculation. */
interface PendingDutyPeriodTemplateFactor {
    fun describe(): String
    fun adjust(item: PendingDutyPeriodTemplate, value: Double): Double
}

data class TieredPendingDutyPeriodTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingDutyPeriodTemplateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingDutyPeriodTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PendingDutyPeriodTemplateStopCountOffsetAdjuster(
    var weight: Double,
) : PendingDutyPeriodTemplateFactor {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: PendingDutyPeriodTemplate, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class MinimumPendingDutyPeriodTemplateAdjuster(var minimum: Double) : PendingDutyPeriodTemplateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingDutyPeriodTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentPendingDutyPeriodTemplateAdjuster(var percent: Double) : PendingDutyPeriodTemplateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingDutyPeriodTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DepotPendingDutyPeriodTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingDutyPeriodTemplateFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: PendingDutyPeriodTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPendingDutyPeriodTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingDutyPeriodTemplateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingDutyPeriodTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the pending duty period template steps in order and keeps a trace of each. */
data class PendingDutyPeriodTemplateEngine(
    var steps: MutableList<PendingDutyPeriodTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingDutyPeriodTemplate): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingDutyPeriodTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingDutyPeriodTemplateEngine(flags: Registry): PendingDutyPeriodTemplateEngine {
    return PendingDutyPeriodTemplateEngine(
        steps = mutableListOf<PendingDutyPeriodTemplateFactor>(
            TieredPendingDutyPeriodTemplateAdjuster(
                mutableListOf<Double>(78.0, 615.0),
                mutableListOf<Double>(1.09, 1.1),
            ),
            PendingDutyPeriodTemplateStopCountOffsetAdjuster(1.82),
            MinimumPendingDutyPeriodTemplateAdjuster(7.3),
            PercentPendingDutyPeriodTemplateAdjuster(-9.2),
            DepotPendingDutyPeriodTemplateAdjuster(mutableMapOf<String, Double>("east" to 1.1)),
            FlaggedPendingDutyPeriodTemplateAdjuster(flags, "pending-duty-period-template-weighted", -5.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 69815.8,
    )
}
