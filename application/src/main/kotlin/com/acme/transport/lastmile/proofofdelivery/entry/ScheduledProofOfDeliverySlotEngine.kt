package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled proof of delivery slot calculation. */
interface ScheduledProofOfDeliverySlotFactor {
    fun describe(): String
    fun adjust(item: ScheduledProofOfDeliverySlot, value: Double): Double
}

data class MinimumScheduledProofOfDeliverySlotAdjuster(
    var minimum: Double,
) : ScheduledProofOfDeliverySlotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledProofOfDeliverySlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedScheduledProofOfDeliverySlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledProofOfDeliverySlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledProofOfDeliverySlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapScheduledProofOfDeliverySlotAdjuster(var cap: Double) : ScheduledProofOfDeliverySlotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledProofOfDeliverySlot, value: Double): Double = minOf(value, cap)
}

/** Runs the scheduled proof of delivery slot steps in order and keeps a trace of each. */
data class ScheduledProofOfDeliverySlotEngine(
    var steps: MutableList<ScheduledProofOfDeliverySlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledProofOfDeliverySlot): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledProofOfDeliverySlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledProofOfDeliverySlotEngine(flags: Registry): ScheduledProofOfDeliverySlotEngine {
    return ScheduledProofOfDeliverySlotEngine(
        steps = mutableListOf<ScheduledProofOfDeliverySlotFactor>(
            MinimumScheduledProofOfDeliverySlotAdjuster(18.8),
            FlaggedScheduledProofOfDeliverySlotAdjuster(
                flags,
                "scheduled-proof-of-delivery-slot-fallback",
                -7.2,
            ),
            CapScheduledProofOfDeliverySlotAdjuster(17432.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 19405.4,
    )
}
