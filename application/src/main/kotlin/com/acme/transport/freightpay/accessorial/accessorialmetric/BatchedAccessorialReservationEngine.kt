package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.kit.flags.Registry

/** One step of the batched accessorial reservation calculation. */
interface BatchedAccessorialReservationModifier {
    fun describe(): String
    fun adjust(item: BatchedAccessorialReservation, value: Double): Double
}

data class RegionBatchedAccessorialReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedAccessorialReservationModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: BatchedAccessorialReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BatchedAccessorialReservationDurationMinutesOffsetAdjuster(
    var weight: Double,
) : BatchedAccessorialReservationModifier {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: BatchedAccessorialReservation, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class FlaggedBatchedAccessorialReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedAccessorialReservationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedAccessorialReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentBatchedAccessorialReservationAdjuster(
    var percent: Double,
) : BatchedAccessorialReservationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedAccessorialReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredBatchedAccessorialReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedAccessorialReservationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedAccessorialReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapBatchedAccessorialReservationAdjuster(var cap: Double) : BatchedAccessorialReservationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedAccessorialReservation, value: Double): Double = minOf(value, cap)
}

/** Runs the batched accessorial reservation steps in order and keeps a trace of each. */
data class BatchedAccessorialReservationEngine(
    var steps: MutableList<BatchedAccessorialReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedAccessorialReservation): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedAccessorialReservationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedAccessorialReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedAccessorialReservationEngine(flags: Registry): BatchedAccessorialReservationEngine {
    return BatchedAccessorialReservationEngine(
        steps = mutableListOf<BatchedAccessorialReservationModifier>(
            RegionBatchedAccessorialReservationAdjuster(
                mutableMapOf<String, Double>("east" to 1.27, "west" to 0.99),
            ),
            BatchedAccessorialReservationDurationMinutesOffsetAdjuster(0.05),
            FlaggedBatchedAccessorialReservationAdjuster(flags, "batched-accessorial-reservation-pending", -5.0),
            PercentBatchedAccessorialReservationAdjuster(1.2),
            TieredBatchedAccessorialReservationAdjuster(
                mutableListOf<Double>(1198.0, 2708.0, 4890.0),
                mutableListOf<Double>(1.11, 1.03, 0.97),
            ),
            CapBatchedAccessorialReservationAdjuster(18484.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 79203.2,
    )
}
