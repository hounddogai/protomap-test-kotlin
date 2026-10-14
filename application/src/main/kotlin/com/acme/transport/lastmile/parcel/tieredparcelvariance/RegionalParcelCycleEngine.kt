package com.acme.transport.lastmile.parcel.tieredparcelvariance

import com.acme.transport.kit.flags.Registry

/** One step of the regional parcel cycle calculation. */
interface RegionalParcelCycleStep {
    fun describe(): String
    fun adjust(item: RegionalParcelCycle, value: Double): Double
}

data class CapRegionalParcelCycleAdjuster(var cap: Double) : RegionalParcelCycleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalParcelCycle, value: Double): Double = minOf(value, cap)
}

data class PercentRegionalParcelCycleAdjuster(var percent: Double) : RegionalParcelCycleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalParcelCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRegionalParcelCycleAdjuster(var minimum: Double) : RegionalParcelCycleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalParcelCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameRegionalParcelCycleAdjuster(var factors: MutableMap<String, Double>) : RegionalParcelCycleStep {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalParcelCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRegionalParcelCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalParcelCycleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalParcelCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional parcel cycle steps in order and keeps a trace of each. */
data class RegionalParcelCycleEngine(
    var steps: MutableList<RegionalParcelCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalParcelCycle): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalParcelCycleStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalParcelCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalParcelCycleEngine(flags: Registry): RegionalParcelCycleEngine {
    return RegionalParcelCycleEngine(
        steps = mutableListOf<RegionalParcelCycleStep>(
            CapRegionalParcelCycleAdjuster(36102.0),
            PercentRegionalParcelCycleAdjuster(18.1),
            MinimumRegionalParcelCycleAdjuster(44.7),
            NameRegionalParcelCycleAdjuster(mutableMapOf<String, Double>("north" to 1.12, "online" to 1.3)),
            FlaggedRegionalParcelCycleAdjuster(flags, "regional-parcel-cycle-pending", -2.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 84398.2,
    )
}
