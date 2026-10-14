package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled fuel surcharge cycle calculation. */
interface ScheduledFuelSurchargeCycleStep {
    fun describe(): String
    fun adjust(item: ScheduledFuelSurchargeCycle, value: Double): Double
}

data class FlaggedScheduledFuelSurchargeCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledFuelSurchargeCycleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledFuelSurchargeCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingScheduledFuelSurchargeCycleAdjuster(var step: Double) : ScheduledFuelSurchargeCycleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledFuelSurchargeCycle, value: Double): Double = roundTo(value, step)
}

data class UnitScheduledFuelSurchargeCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledFuelSurchargeCycleStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: ScheduledFuelSurchargeCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled fuel surcharge cycle steps in order and keeps a trace of each. */
data class ScheduledFuelSurchargeCycleEngine(
    var steps: MutableList<ScheduledFuelSurchargeCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledFuelSurchargeCycle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledFuelSurchargeCycleStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledFuelSurchargeCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledFuelSurchargeCycleEngine(flags: Registry): ScheduledFuelSurchargeCycleEngine {
    return ScheduledFuelSurchargeCycleEngine(
        steps = mutableListOf<ScheduledFuelSurchargeCycleStep>(
            FlaggedScheduledFuelSurchargeCycleAdjuster(flags, "scheduled-fuel-surcharge-cycle-strict", 3.2),
            RoundingScheduledFuelSurchargeCycleAdjuster(5.0),
            UnitScheduledFuelSurchargeCycleAdjuster(
                mutableMapOf<String, Double>("north" to 0.72, "west" to 1.1, "south" to 1.24),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 30444.9,
    )
}
