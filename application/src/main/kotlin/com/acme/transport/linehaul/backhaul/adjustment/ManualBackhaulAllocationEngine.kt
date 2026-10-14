package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual backhaul allocation calculation. */
interface ManualBackhaulAllocationStep {
    fun describe(): String
    fun adjust(item: ManualBackhaulAllocation, value: Double): Double
}

data class RoundingManualBackhaulAllocationAdjuster(var step: Double) : ManualBackhaulAllocationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualBackhaulAllocation, value: Double): Double = roundTo(value, step)
}

data class TieredManualBackhaulAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualBackhaulAllocationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualBackhaulAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedManualBackhaulAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualBackhaulAllocationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualBackhaulAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ManualBackhaulAllocationStartsAtOffsetAdjuster(var weight: Double) : ManualBackhaulAllocationStep {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: ManualBackhaulAllocation, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class NameManualBackhaulAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualBackhaulAllocationStep {
    override fun describe(): String = "by name"

    override fun adjust(item: ManualBackhaulAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumManualBackhaulAllocationAdjuster(var minimum: Double) : ManualBackhaulAllocationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualBackhaulAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual backhaul allocation steps in order and keeps a trace of each. */
data class ManualBackhaulAllocationEngine(
    var steps: MutableList<ManualBackhaulAllocationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualBackhaulAllocation): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualBackhaulAllocationStep = steps[index]
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

    fun evaluateAll(items: MutableList<ManualBackhaulAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualBackhaulAllocationEngine(flags: Registry): ManualBackhaulAllocationEngine {
    return ManualBackhaulAllocationEngine(
        steps = mutableListOf<ManualBackhaulAllocationStep>(
            RoundingManualBackhaulAllocationAdjuster(0.05),
            TieredManualBackhaulAllocationAdjuster(
                mutableListOf<Double>(119.0, 2475.0),
                mutableListOf<Double>(1.09, 1.04),
            ),
            FlaggedManualBackhaulAllocationAdjuster(flags, "manual-backhaul-allocation-scheduled", -2.2),
            ManualBackhaulAllocationStartsAtOffsetAdjuster(0.68),
            NameManualBackhaulAllocationAdjuster(
                mutableMapOf<String, Double>("east" to 1.37, "north" to 1.39, "online" to 1.29),
            ),
            MinimumManualBackhaulAllocationAdjuster(23.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 4517.7,
    )
}
