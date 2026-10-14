package com.acme.transport.network.transittime.scheduledtimebatch

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled transit time batch calculation. */
interface ScheduledTransitTimeBatchStep {
    fun describe(): String
    fun adjust(item: ScheduledTransitTimeBatch, value: Double): Double
}

data class CapScheduledTransitTimeBatchAdjuster(var cap: Double) : ScheduledTransitTimeBatchStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledTransitTimeBatch, value: Double): Double = minOf(value, cap)
}

data class TieredScheduledTransitTimeBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledTransitTimeBatchStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledTransitTimeBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedScheduledTransitTimeBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledTransitTimeBatchStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledTransitTimeBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ScheduledTransitTimeBatchRatioOffsetAdjuster(var weight: Double) : ScheduledTransitTimeBatchStep {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: ScheduledTransitTimeBatch, value: Double): Double = value + item.ratio * weight
}

/** Runs the scheduled transit time batch steps in order and keeps a trace of each. */
data class ScheduledTransitTimeBatchEngine(
    var steps: MutableList<ScheduledTransitTimeBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledTransitTimeBatch): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledTransitTimeBatchStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledTransitTimeBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledTransitTimeBatchEngine(flags: Registry): ScheduledTransitTimeBatchEngine {
    return ScheduledTransitTimeBatchEngine(
        steps = mutableListOf<ScheduledTransitTimeBatchStep>(
            CapScheduledTransitTimeBatchAdjuster(35995.0),
            TieredScheduledTransitTimeBatchAdjuster(
                mutableListOf<Double>(4003.0, 4016.0, 6578.0, 8616.0),
                mutableListOf<Double>(1.12, 1.14, 1.16, 1.0),
            ),
            FlaggedScheduledTransitTimeBatchAdjuster(flags, "scheduled-transit-time-batch-seasonal", 5.9),
            ScheduledTransitTimeBatchRatioOffsetAdjuster(0.51),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 60591.8,
    )
}
