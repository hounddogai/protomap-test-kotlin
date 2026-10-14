package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled dwell allocation calculation. */
interface ScheduledDwellAllocationFactor {
    fun describe(): String
    fun adjust(item: ScheduledDwellAllocation, value: Double): Double
}

data class ScheduledDwellAllocationEtaMinutesOffsetAdjuster(
    var weight: Double,
) : ScheduledDwellAllocationFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: ScheduledDwellAllocation, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class MinimumScheduledDwellAllocationAdjuster(var minimum: Double) : ScheduledDwellAllocationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledDwellAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CategoryScheduledDwellAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledDwellAllocationFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: ScheduledDwellAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedScheduledDwellAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledDwellAllocationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledDwellAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentScheduledDwellAllocationAdjuster(var percent: Double) : ScheduledDwellAllocationFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledDwellAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredScheduledDwellAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledDwellAllocationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledDwellAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled dwell allocation steps in order and keeps a trace of each. */
data class ScheduledDwellAllocationEngine(
    var steps: MutableList<ScheduledDwellAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledDwellAllocation): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledDwellAllocationFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledDwellAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledDwellAllocationEngine(flags: Registry): ScheduledDwellAllocationEngine {
    return ScheduledDwellAllocationEngine(
        steps = mutableListOf<ScheduledDwellAllocationFactor>(
            ScheduledDwellAllocationEtaMinutesOffsetAdjuster(1.99),
            MinimumScheduledDwellAllocationAdjuster(14.7),
            CategoryScheduledDwellAllocationAdjuster(
                mutableMapOf<String, Double>("retail" to 0.91, "central" to 1.02, "online" to 1.34),
            ),
            FlaggedScheduledDwellAllocationAdjuster(flags, "scheduled-dwell-allocation-strict", -1.7),
            PercentScheduledDwellAllocationAdjuster(14.2),
            TieredScheduledDwellAllocationAdjuster(
                mutableListOf<Double>(7251.0, 8385.0, 9369.0),
                mutableListOf<Double>(0.83, 1.06, 1.15),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 22986.1,
    )
}
