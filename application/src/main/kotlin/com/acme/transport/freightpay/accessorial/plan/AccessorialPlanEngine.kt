package com.acme.transport.freightpay.accessorial.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the accessorial plan calculation. */
interface AccessorialPlanStep {
    fun describe(): String
    fun adjust(item: AccessorialPlan, value: Double): Double
}

data class DepotAccessorialPlanAdjuster(var factors: MutableMap<String, Double>) : AccessorialPlanStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: AccessorialPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingAccessorialPlanAdjuster(var step: Double) : AccessorialPlanStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AccessorialPlan, value: Double): Double = roundTo(value, step)
}

data class FlaggedAccessorialPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AccessorialPlanStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AccessorialPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class AccessorialPlanQuantityOffsetAdjuster(var weight: Double) : AccessorialPlanStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: AccessorialPlan, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class CapAccessorialPlanAdjuster(var cap: Double) : AccessorialPlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AccessorialPlan, value: Double): Double = minOf(value, cap)
}

/** Runs the accessorial plan steps in order and keeps a trace of each. */
data class AccessorialPlanEngine(
    var steps: MutableList<AccessorialPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AccessorialPlan): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AccessorialPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAccessorialPlanEngine(flags: Registry): AccessorialPlanEngine {
    return AccessorialPlanEngine(
        steps = mutableListOf<AccessorialPlanStep>(
            DepotAccessorialPlanAdjuster(mutableMapOf<String, Double>("online" to 0.78)),
            RoundingAccessorialPlanAdjuster(0.1),
            FlaggedAccessorialPlanAdjuster(flags, "accessorial-plan-batched", 4.2),
            AccessorialPlanQuantityOffsetAdjuster(0.99),
            CapAccessorialPlanAdjuster(27298.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 3812.1,
    )
}
