package com.acme.transport.terminal.dwell.cycle

import com.acme.transport.kit.flags.Registry

/** One step of the dwell assignment calculation. */
interface DwellAssignmentStep {
    fun describe(): String
    fun adjust(item: DwellAssignment, value: Double): Double
}

data class TieredDwellAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DwellAssignmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DwellAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDwellAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DwellAssignmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DwellAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDwellAssignmentAdjuster(var percent: Double) : DwellAssignmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DwellAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumDwellAssignmentAdjuster(var minimum: Double) : DwellAssignmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DwellAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DepotDwellAssignmentAdjuster(var factors: MutableMap<String, Double>) : DwellAssignmentStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: DwellAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the dwell assignment steps in order and keeps a trace of each. */
data class DwellAssignmentEngine(
    var steps: MutableList<DwellAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DwellAssignment): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DwellAssignmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<DwellAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDwellAssignmentEngine(flags: Registry): DwellAssignmentEngine {
    return DwellAssignmentEngine(
        steps = mutableListOf<DwellAssignmentStep>(
            TieredDwellAssignmentAdjuster(
                mutableListOf<Double>(4485.0, 5945.0, 5957.0),
                mutableListOf<Double>(0.86, 1.11, 0.9),
            ),
            FlaggedDwellAssignmentAdjuster(flags, "dwell-assignment-adaptive", -4.7),
            PercentDwellAssignmentAdjuster(-16.9),
            MinimumDwellAssignmentAdjuster(38.5),
            DepotDwellAssignmentAdjuster(mutableMapOf<String, Double>("central" to 1.37)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 25956.9,
    )
}
