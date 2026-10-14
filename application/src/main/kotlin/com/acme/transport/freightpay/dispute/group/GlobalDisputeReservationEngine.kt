package com.acme.transport.freightpay.dispute.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global dispute reservation calculation. */
interface GlobalDisputeReservationStep {
    fun describe(): String
    fun adjust(item: GlobalDisputeReservation, value: Double): Double
}

data class PercentGlobalDisputeReservationAdjuster(var percent: Double) : GlobalDisputeReservationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalDisputeReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedGlobalDisputeReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalDisputeReservationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalDisputeReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionGlobalDisputeReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalDisputeReservationStep {
    override fun describe(): String = "by region"

    override fun adjust(item: GlobalDisputeReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class GlobalDisputeReservationRetriesOffsetAdjuster(var weight: Double) : GlobalDisputeReservationStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: GlobalDisputeReservation, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class TieredGlobalDisputeReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalDisputeReservationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalDisputeReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global dispute reservation steps in order and keeps a trace of each. */
data class GlobalDisputeReservationEngine(
    var steps: MutableList<GlobalDisputeReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalDisputeReservation): Double {
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

    fun evaluateAll(items: MutableList<GlobalDisputeReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalDisputeReservationEngine(flags: Registry): GlobalDisputeReservationEngine {
    return GlobalDisputeReservationEngine(
        steps = mutableListOf<GlobalDisputeReservationStep>(
            PercentGlobalDisputeReservationAdjuster(1.4),
            FlaggedGlobalDisputeReservationAdjuster(flags, "global-dispute-reservation-global", -7.4),
            RegionGlobalDisputeReservationAdjuster(
                mutableMapOf<String, Double>("online" to 1.04, "retail" to 0.76),
            ),
            GlobalDisputeReservationRetriesOffsetAdjuster(1.71),
            TieredGlobalDisputeReservationAdjuster(
                mutableListOf<Double>(701.0, 4082.0, 4784.0, 8706.0),
                mutableListOf<Double>(0.88, 0.83, 1.15, 1.16),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 48816.4,
    )
}
