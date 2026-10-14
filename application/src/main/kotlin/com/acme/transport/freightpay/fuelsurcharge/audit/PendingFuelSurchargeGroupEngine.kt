package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending fuel surcharge group calculation. */
interface PendingFuelSurchargeGroupModifier {
    fun describe(): String
    fun adjust(item: PendingFuelSurchargeGroup, value: Double): Double
}

data class PercentPendingFuelSurchargeGroupAdjuster(var percent: Double) : PendingFuelSurchargeGroupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingFuelSurchargeGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class UnitPendingFuelSurchargeGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingFuelSurchargeGroupModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: PendingFuelSurchargeGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingPendingFuelSurchargeGroupAdjuster(var step: Double) : PendingFuelSurchargeGroupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingFuelSurchargeGroup, value: Double): Double = roundTo(value, step)
}

data class MinimumPendingFuelSurchargeGroupAdjuster(var minimum: Double) : PendingFuelSurchargeGroupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingFuelSurchargeGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPendingFuelSurchargeGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingFuelSurchargeGroupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingFuelSurchargeGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the pending fuel surcharge group steps in order and keeps a trace of each. */
data class PendingFuelSurchargeGroupEngine(
    var steps: MutableList<PendingFuelSurchargeGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingFuelSurchargeGroup): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingFuelSurchargeGroupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingFuelSurchargeGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingFuelSurchargeGroupEngine(flags: Registry): PendingFuelSurchargeGroupEngine {
    return PendingFuelSurchargeGroupEngine(
        steps = mutableListOf<PendingFuelSurchargeGroupModifier>(
            PercentPendingFuelSurchargeGroupAdjuster(-7.0),
            UnitPendingFuelSurchargeGroupAdjuster(mutableMapOf<String, Double>("online" to 1.2)),
            RoundingPendingFuelSurchargeGroupAdjuster(0.05),
            MinimumPendingFuelSurchargeGroupAdjuster(39.9),
            FlaggedPendingFuelSurchargeGroupAdjuster(flags, "pending-fuel-surcharge-group-tiered", -6.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 5726.6,
    )
}
