package com.acme.transport.lastmile.proofofdelivery.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global proof of delivery allocation calculation. */
interface GlobalProofOfDeliveryAllocationAdjuster {
    fun describe(): String
    fun adjust(item: GlobalProofOfDeliveryAllocation, value: Double): Double
}

data class TieredGlobalProofOfDeliveryAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalProofOfDeliveryAllocationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalProofOfDeliveryAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingGlobalProofOfDeliveryAllocationAdjuster(
    var step: Double,
) : GlobalProofOfDeliveryAllocationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalProofOfDeliveryAllocation, value: Double): Double = roundTo(value, step)
}

data class GlobalProofOfDeliveryAllocationMinimumOffsetAdjuster(
    var weight: Double,
) : GlobalProofOfDeliveryAllocationAdjuster {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: GlobalProofOfDeliveryAllocation, value: Double): Double = value + item.minimum * weight
}

data class ChannelGlobalProofOfDeliveryAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalProofOfDeliveryAllocationAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: GlobalProofOfDeliveryAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapGlobalProofOfDeliveryAllocationAdjuster(
    var cap: Double,
) : GlobalProofOfDeliveryAllocationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalProofOfDeliveryAllocation, value: Double): Double = minOf(value, cap)
}

data class FlaggedGlobalProofOfDeliveryAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalProofOfDeliveryAllocationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalProofOfDeliveryAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global proof of delivery allocation steps in order and keeps a trace of each. */
data class GlobalProofOfDeliveryAllocationEngine(
    var steps: MutableList<GlobalProofOfDeliveryAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalProofOfDeliveryAllocation): Double {
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

    fun evaluateAll(items: MutableList<GlobalProofOfDeliveryAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalProofOfDeliveryAllocationEngine(flags: Registry): GlobalProofOfDeliveryAllocationEngine {
    return GlobalProofOfDeliveryAllocationEngine(
        steps = mutableListOf<GlobalProofOfDeliveryAllocationAdjuster>(
            TieredGlobalProofOfDeliveryAllocationAdjuster(
                mutableListOf<Double>(44.0, 2513.0, 3651.0),
                mutableListOf<Double>(0.91, 1.06, 1.02),
            ),
            RoundingGlobalProofOfDeliveryAllocationAdjuster(0.1),
            GlobalProofOfDeliveryAllocationMinimumOffsetAdjuster(0.84),
            ChannelGlobalProofOfDeliveryAllocationAdjuster(
                mutableMapOf<String, Double>("retail" to 1.24, "central" to 1.2),
            ),
            CapGlobalProofOfDeliveryAllocationAdjuster(21937.0),
            FlaggedGlobalProofOfDeliveryAllocationAdjuster(
                flags,
                "global-proof-of-delivery-allocation-regional",
                -8.6,
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 71880.8,
    )
}
