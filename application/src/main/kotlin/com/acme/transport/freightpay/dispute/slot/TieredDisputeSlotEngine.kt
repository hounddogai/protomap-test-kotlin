package com.acme.transport.freightpay.dispute.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered dispute slot calculation. */
interface TieredDisputeSlotModifier {
    fun describe(): String
    fun adjust(item: TieredDisputeSlot, value: Double): Double
}

data class TieredDisputeSlotCostOffsetAdjuster(var weight: Double) : TieredDisputeSlotModifier {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: TieredDisputeSlot, value: Double): Double = value + item.cost * weight
}

data class FlaggedTieredDisputeSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredDisputeSlotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredDisputeSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTieredDisputeSlotAdjuster(var percent: Double) : TieredDisputeSlotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredDisputeSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingTieredDisputeSlotAdjuster(var step: Double) : TieredDisputeSlotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredDisputeSlot, value: Double): Double = roundTo(value, step)
}

data class LabelTieredDisputeSlotAdjuster(var factors: MutableMap<String, Double>) : TieredDisputeSlotModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: TieredDisputeSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapTieredDisputeSlotAdjuster(var cap: Double) : TieredDisputeSlotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredDisputeSlot, value: Double): Double = minOf(value, cap)
}

/** Runs the tiered dispute slot steps in order and keeps a trace of each. */
data class TieredDisputeSlotEngine(
    var steps: MutableList<TieredDisputeSlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredDisputeSlot): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredDisputeSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredDisputeSlotEngine(flags: Registry): TieredDisputeSlotEngine {
    return TieredDisputeSlotEngine(
        steps = mutableListOf<TieredDisputeSlotModifier>(
            TieredDisputeSlotCostOffsetAdjuster(0.69),
            FlaggedTieredDisputeSlotAdjuster(flags, "tiered-dispute-slot-capped", -5.3),
            PercentTieredDisputeSlotAdjuster(-15.4),
            RoundingTieredDisputeSlotAdjuster(0.25),
            LabelTieredDisputeSlotAdjuster(
                mutableMapOf<String, Double>("online" to 0.7, "south" to 1.36, "east" to 0.95),
            ),
            CapTieredDisputeSlotAdjuster(18497.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 47407.3,
    )
}
