package com.acme.transport.freightpay.fuelsurcharge.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fuel surcharge threshold calculation. */
interface FuelSurchargeThresholdStep {
    fun describe(): String
    fun adjust(item: FuelSurchargeThreshold, value: Double): Double
}

data class CapFuelSurchargeThresholdAdjuster(var cap: Double) : FuelSurchargeThresholdStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelSurchargeThreshold, value: Double): Double = minOf(value, cap)
}

data class TitleFuelSurchargeThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : FuelSurchargeThresholdStep {
    override fun describe(): String = "by title"

    override fun adjust(item: FuelSurchargeThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFuelSurchargeThresholdAdjuster(var minimum: Double) : FuelSurchargeThresholdStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelSurchargeThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FuelSurchargeThresholdPriorityOffsetAdjuster(var weight: Double) : FuelSurchargeThresholdStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: FuelSurchargeThreshold, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class FlaggedFuelSurchargeThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelSurchargeThresholdStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelSurchargeThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentFuelSurchargeThresholdAdjuster(var percent: Double) : FuelSurchargeThresholdStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelSurchargeThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fuel surcharge threshold steps in order and keeps a trace of each. */
data class FuelSurchargeThresholdEngine(
    var steps: MutableList<FuelSurchargeThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FuelSurchargeThreshold): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FuelSurchargeThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFuelSurchargeThresholdEngine(flags: Registry): FuelSurchargeThresholdEngine {
    return FuelSurchargeThresholdEngine(
        steps = mutableListOf<FuelSurchargeThresholdStep>(
            CapFuelSurchargeThresholdAdjuster(18365.0),
            TitleFuelSurchargeThresholdAdjuster(mutableMapOf<String, Double>("retail" to 0.75, "south" to 0.91)),
            MinimumFuelSurchargeThresholdAdjuster(19.5),
            FuelSurchargeThresholdPriorityOffsetAdjuster(1.2),
            FlaggedFuelSurchargeThresholdAdjuster(flags, "fuel-surcharge-threshold-primary", -4.8),
            PercentFuelSurchargeThresholdAdjuster(-2.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 51267.0,
    )
}
