package com.acme.transport.freightpay.fuelsurcharge.target

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled fuel surcharge request calculation. */
interface ScheduledFuelSurchargeRequestAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledFuelSurchargeRequest, value: Double): Double
}

data class PercentScheduledFuelSurchargeRequestAdjuster(
    var percent: Double,
) : ScheduledFuelSurchargeRequestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledFuelSurchargeRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumScheduledFuelSurchargeRequestAdjuster(
    var minimum: Double,
) : ScheduledFuelSurchargeRequestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledFuelSurchargeRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingScheduledFuelSurchargeRequestAdjuster(
    var step: Double,
) : ScheduledFuelSurchargeRequestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledFuelSurchargeRequest, value: Double): Double = roundTo(value, step)
}

data class TieredScheduledFuelSurchargeRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledFuelSurchargeRequestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledFuelSurchargeRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled fuel surcharge request steps in order and keeps a trace of each. */
data class ScheduledFuelSurchargeRequestEngine(
    var steps: MutableList<ScheduledFuelSurchargeRequestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledFuelSurchargeRequest): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledFuelSurchargeRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledFuelSurchargeRequestEngine(): ScheduledFuelSurchargeRequestEngine {
    return ScheduledFuelSurchargeRequestEngine(
        steps = mutableListOf<ScheduledFuelSurchargeRequestAdjuster>(
            PercentScheduledFuelSurchargeRequestAdjuster(-10.8),
            MinimumScheduledFuelSurchargeRequestAdjuster(28.9),
            RoundingScheduledFuelSurchargeRequestAdjuster(0.1),
            TieredScheduledFuelSurchargeRequestAdjuster(
                mutableListOf<Double>(3822.0, 8553.0, 8835.0),
                mutableListOf<Double>(1.04, 1.0, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 2710.8,
    )
}
