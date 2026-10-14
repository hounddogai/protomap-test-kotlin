package com.acme.transport.network.transittime.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the transit time slot calculation. */
interface TransitTimeSlotModifier {
    fun describe(): String
    fun adjust(item: TransitTimeSlot, value: Double): Double
}

data class FlaggedTransitTimeSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TransitTimeSlotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TransitTimeSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTransitTimeSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TransitTimeSlotModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TransitTimeSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTransitTimeSlotAdjuster(var cap: Double) : TransitTimeSlotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TransitTimeSlot, value: Double): Double = minOf(value, cap)
}

data class RoundingTransitTimeSlotAdjuster(var step: Double) : TransitTimeSlotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TransitTimeSlot, value: Double): Double = roundTo(value, step)
}

data class PercentTransitTimeSlotAdjuster(var percent: Double) : TransitTimeSlotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TransitTimeSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleTransitTimeSlotAdjuster(var factors: MutableMap<String, Double>) : TransitTimeSlotModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: TransitTimeSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the transit time slot steps in order and keeps a trace of each. */
data class TransitTimeSlotEngine(
    var steps: MutableList<TransitTimeSlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TransitTimeSlot): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TransitTimeSlotModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TransitTimeSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTransitTimeSlotEngine(flags: Registry): TransitTimeSlotEngine {
    return TransitTimeSlotEngine(
        steps = mutableListOf<TransitTimeSlotModifier>(
            FlaggedTransitTimeSlotAdjuster(flags, "transit-time-slot-regional", -9.7),
            TieredTransitTimeSlotAdjuster(
                mutableListOf<Double>(845.0, 2850.0, 3810.0, 7446.0),
                mutableListOf<Double>(1.01, 0.97, 1.2, 1.05),
            ),
            CapTransitTimeSlotAdjuster(34025.0),
            RoundingTransitTimeSlotAdjuster(0.05),
            PercentTransitTimeSlotAdjuster(14.5),
            TitleTransitTimeSlotAdjuster(
                mutableMapOf<String, Double>("south" to 1.2, "north" to 1.25, "west" to 1.08),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 75830.2,
    )
}
