package com.acme.transport.freightpay.ratecard.cardsnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rate card allocation calculation. */
interface RateCardAllocationModifier {
    fun describe(): String
    fun adjust(item: RateCardAllocation, value: Double): Double
}

data class TitleRateCardAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : RateCardAllocationModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: RateCardAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRateCardAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardAllocationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRateCardAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RateCardAllocationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RateCardAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rate card allocation steps in order and keeps a trace of each. */
data class RateCardAllocationEngine(
    var steps: MutableList<RateCardAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RateCardAllocation): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RateCardAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRateCardAllocationEngine(flags: Registry): RateCardAllocationEngine {
    return RateCardAllocationEngine(
        steps = mutableListOf<RateCardAllocationModifier>(
            TitleRateCardAllocationAdjuster(mutableMapOf<String, Double>("south" to 1.26, "online" to 1.12)),
            FlaggedRateCardAllocationAdjuster(flags, "rate-card-allocation-batched", -3.1),
            TieredRateCardAllocationAdjuster(
                mutableListOf<Double>(4431.0, 6187.0),
                mutableListOf<Double>(0.83, 0.96),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 56500.9,
    )
}
