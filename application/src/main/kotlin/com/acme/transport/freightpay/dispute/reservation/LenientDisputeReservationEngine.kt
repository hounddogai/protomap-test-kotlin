package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient dispute reservation calculation. */
interface LenientDisputeReservationStep {
    fun describe(): String
    fun adjust(item: LenientDisputeReservation, value: Double): Double
}

data class FlaggedLenientDisputeReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientDisputeReservationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientDisputeReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLenientDisputeReservationAdjuster(var percent: Double) : LenientDisputeReservationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientDisputeReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLenientDisputeReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientDisputeReservationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientDisputeReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SourceLenientDisputeReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientDisputeReservationStep {
    override fun describe(): String = "by source"

    override fun adjust(item: LenientDisputeReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient dispute reservation steps in order and keeps a trace of each. */
data class LenientDisputeReservationEngine(
    var steps: MutableList<LenientDisputeReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientDisputeReservation): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientDisputeReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientDisputeReservationEngine(flags: Registry): LenientDisputeReservationEngine {
    return LenientDisputeReservationEngine(
        steps = mutableListOf<LenientDisputeReservationStep>(
            FlaggedLenientDisputeReservationAdjuster(flags, "lenient-dispute-reservation-lenient", 8.5),
            PercentLenientDisputeReservationAdjuster(-24.7),
            TieredLenientDisputeReservationAdjuster(
                mutableListOf<Double>(5488.0, 5800.0, 6274.0),
                mutableListOf<Double>(0.92, 1.2, 1.0),
            ),
            SourceLenientDisputeReservationAdjuster(mutableMapOf<String, Double>("west" to 1.18)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 21644.1,
    )
}
