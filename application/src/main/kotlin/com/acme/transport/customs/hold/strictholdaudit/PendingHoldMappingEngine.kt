package com.acme.transport.customs.hold.strictholdaudit

import com.acme.transport.kit.flags.Registry

/** One step of the pending hold mapping calculation. */
interface PendingHoldMappingAdjuster {
    fun describe(): String
    fun adjust(item: PendingHoldMapping, value: Double): Double
}

data class TieredPendingHoldMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingHoldMappingAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingHoldMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentPendingHoldMappingAdjuster(var percent: Double) : PendingHoldMappingAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingHoldMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPendingHoldMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingHoldMappingAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingHoldMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPendingHoldMappingAdjuster(var minimum: Double) : PendingHoldMappingAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingHoldMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PendingHoldMappingDurationMinutesOffsetAdjuster(var weight: Double) : PendingHoldMappingAdjuster {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: PendingHoldMapping, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

/** Runs the pending hold mapping steps in order and keeps a trace of each. */
data class PendingHoldMappingEngine(
    var steps: MutableList<PendingHoldMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingHoldMapping): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingHoldMappingAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingHoldMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingHoldMappingEngine(flags: Registry): PendingHoldMappingEngine {
    return PendingHoldMappingEngine(
        steps = mutableListOf<PendingHoldMappingAdjuster>(
            TieredPendingHoldMappingAdjuster(
                mutableListOf<Double>(5744.0, 9485.0),
                mutableListOf<Double>(0.86, 0.95),
            ),
            PercentPendingHoldMappingAdjuster(6.7),
            FlaggedPendingHoldMappingAdjuster(flags, "pending-hold-mapping-rolling", -5.0),
            MinimumPendingHoldMappingAdjuster(26.6),
            PendingHoldMappingDurationMinutesOffsetAdjuster(1.24),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 26585.2,
    )
}
