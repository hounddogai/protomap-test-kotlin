package com.acme.transport.freightpay.fuelsurcharge.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel surcharge schedule calculation. */
interface FuelSurchargeScheduleStep {
    fun describe(): String
    fun adjust(item: FuelSurchargeSchedule, value: Double): Double
}

data class LocaleFuelSurchargeScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : FuelSurchargeScheduleStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: FuelSurchargeSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingFuelSurchargeScheduleAdjuster(var step: Double) : FuelSurchargeScheduleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelSurchargeSchedule, value: Double): Double = roundTo(value, step)
}

data class PercentFuelSurchargeScheduleAdjuster(var percent: Double) : FuelSurchargeScheduleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelSurchargeSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFuelSurchargeScheduleAdjuster(var cap: Double) : FuelSurchargeScheduleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelSurchargeSchedule, value: Double): Double = minOf(value, cap)
}

data class TieredFuelSurchargeScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelSurchargeScheduleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelSurchargeSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedFuelSurchargeScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelSurchargeScheduleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelSurchargeSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fuel surcharge schedule steps in order and keeps a trace of each. */
data class FuelSurchargeScheduleEngine(
    var steps: MutableList<FuelSurchargeScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FuelSurchargeSchedule): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelSurchargeScheduleStep = steps[index]
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

    fun evaluateAll(items: MutableList<FuelSurchargeSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFuelSurchargeScheduleEngine(flags: Registry): FuelSurchargeScheduleEngine {
    return FuelSurchargeScheduleEngine(
        steps = mutableListOf<FuelSurchargeScheduleStep>(
            LocaleFuelSurchargeScheduleAdjuster(
                mutableMapOf<String, Double>("north" to 1.15, "west" to 1.33, "east" to 0.94),
            ),
            RoundingFuelSurchargeScheduleAdjuster(0.05),
            PercentFuelSurchargeScheduleAdjuster(-5.5),
            CapFuelSurchargeScheduleAdjuster(13441.0),
            TieredFuelSurchargeScheduleAdjuster(
                mutableListOf<Double>(4712.0, 6471.0),
                mutableListOf<Double>(0.91, 0.99),
            ),
            FlaggedFuelSurchargeScheduleAdjuster(flags, "fuel-surcharge-schedule-manual", -4.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 90310.5,
    )
}
