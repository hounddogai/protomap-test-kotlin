package com.acme.transport.safety.violation.manualviolationquota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict violation quota calculation. */
interface StrictViolationQuotaStep {
    fun describe(): String
    fun adjust(item: StrictViolationQuota, value: Double): Double
}

data class FlaggedStrictViolationQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictViolationQuotaStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictViolationQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class StrictViolationQuotaGraceDaysOffsetAdjuster(var weight: Double) : StrictViolationQuotaStep {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: StrictViolationQuota, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class TitleStrictViolationQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictViolationQuotaStep {
    override fun describe(): String = "by title"

    override fun adjust(item: StrictViolationQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingStrictViolationQuotaAdjuster(var step: Double) : StrictViolationQuotaStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictViolationQuota, value: Double): Double = roundTo(value, step)
}

data class TieredStrictViolationQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictViolationQuotaStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictViolationQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapStrictViolationQuotaAdjuster(var cap: Double) : StrictViolationQuotaStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictViolationQuota, value: Double): Double = minOf(value, cap)
}

/** Runs the strict violation quota steps in order and keeps a trace of each. */
data class StrictViolationQuotaEngine(
    var steps: MutableList<StrictViolationQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictViolationQuota): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictViolationQuotaStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictViolationQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictViolationQuotaEngine(flags: Registry): StrictViolationQuotaEngine {
    return StrictViolationQuotaEngine(
        steps = mutableListOf<StrictViolationQuotaStep>(
            FlaggedStrictViolationQuotaAdjuster(flags, "strict-violation-quota-pending", -1.6),
            StrictViolationQuotaGraceDaysOffsetAdjuster(0.83),
            TitleStrictViolationQuotaAdjuster(
                mutableMapOf<String, Double>("east" to 0.87, "south" to 0.93, "north" to 0.86),
            ),
            RoundingStrictViolationQuotaAdjuster(0.05),
            TieredStrictViolationQuotaAdjuster(
                mutableListOf<Double>(3656.0, 4874.0),
                mutableListOf<Double>(0.87, 0.87),
            ),
            CapStrictViolationQuotaAdjuster(18514.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 29454.7,
    )
}
