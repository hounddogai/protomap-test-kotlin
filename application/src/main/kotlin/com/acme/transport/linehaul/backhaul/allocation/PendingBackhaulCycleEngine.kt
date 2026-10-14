package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending backhaul cycle calculation. */
interface PendingBackhaulCycleStep {
    fun describe(): String
    fun adjust(item: PendingBackhaulCycle, value: Double): Double
}

data class MinimumPendingBackhaulCycleAdjuster(var minimum: Double) : PendingBackhaulCycleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingBackhaulCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPendingBackhaulCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingBackhaulCycleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingBackhaulCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPendingBackhaulCycleAdjuster(var step: Double) : PendingBackhaulCycleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingBackhaulCycle, value: Double): Double = roundTo(value, step)
}

data class CapPendingBackhaulCycleAdjuster(var cap: Double) : PendingBackhaulCycleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingBackhaulCycle, value: Double): Double = minOf(value, cap)
}

/** Runs the pending backhaul cycle steps in order and keeps a trace of each. */
data class PendingBackhaulCycleEngine(
    var steps: MutableList<PendingBackhaulCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingBackhaulCycle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingBackhaulCycleStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingBackhaulCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingBackhaulCycleEngine(flags: Registry): PendingBackhaulCycleEngine {
    return PendingBackhaulCycleEngine(
        steps = mutableListOf<PendingBackhaulCycleStep>(
            MinimumPendingBackhaulCycleAdjuster(27.2),
            FlaggedPendingBackhaulCycleAdjuster(flags, "pending-backhaul-cycle-default", 7.5),
            RoundingPendingBackhaulCycleAdjuster(0.05),
            CapPendingBackhaulCycleAdjuster(34780.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 54513.4,
    )
}
