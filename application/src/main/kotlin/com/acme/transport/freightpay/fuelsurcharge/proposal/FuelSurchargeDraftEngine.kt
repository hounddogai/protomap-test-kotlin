package com.acme.transport.freightpay.fuelsurcharge.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel surcharge draft calculation. */
interface FuelSurchargeDraftModifier {
    fun describe(): String
    fun adjust(item: FuelSurchargeDraft, value: Double): Double
}

data class FlaggedFuelSurchargeDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelSurchargeDraftModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelSurchargeDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class FuelSurchargeDraftThresholdOffsetAdjuster(var weight: Double) : FuelSurchargeDraftModifier {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: FuelSurchargeDraft, value: Double): Double = value + item.threshold * weight
}

data class MinimumFuelSurchargeDraftAdjuster(var minimum: Double) : FuelSurchargeDraftModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelSurchargeDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFuelSurchargeDraftAdjuster(var step: Double) : FuelSurchargeDraftModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelSurchargeDraft, value: Double): Double = roundTo(value, step)
}

data class SourceFuelSurchargeDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : FuelSurchargeDraftModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: FuelSurchargeDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapFuelSurchargeDraftAdjuster(var cap: Double) : FuelSurchargeDraftModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelSurchargeDraft, value: Double): Double = minOf(value, cap)
}

/** Runs the fuel surcharge draft steps in order and keeps a trace of each. */
data class FuelSurchargeDraftEngine(
    var steps: MutableList<FuelSurchargeDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FuelSurchargeDraft): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelSurchargeDraftModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FuelSurchargeDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFuelSurchargeDraftEngine(flags: Registry): FuelSurchargeDraftEngine {
    return FuelSurchargeDraftEngine(
        steps = mutableListOf<FuelSurchargeDraftModifier>(
            FlaggedFuelSurchargeDraftAdjuster(flags, "fuel-surcharge-draft-primary", -3.7),
            FuelSurchargeDraftThresholdOffsetAdjuster(0.12),
            MinimumFuelSurchargeDraftAdjuster(13.9),
            RoundingFuelSurchargeDraftAdjuster(1.0),
            SourceFuelSurchargeDraftAdjuster(
                mutableMapOf<String, Double>("north" to 1.35, "west" to 1.02, "east" to 1.0),
            ),
            CapFuelSurchargeDraftAdjuster(39993.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 42014.2,
    )
}
