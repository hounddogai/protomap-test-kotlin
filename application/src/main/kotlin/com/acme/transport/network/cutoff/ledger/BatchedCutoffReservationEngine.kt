package com.acme.transport.network.cutoff.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched cutoff reservation calculation. */
interface BatchedCutoffReservationAdjuster {
    fun describe(): String
    fun adjust(item: BatchedCutoffReservation, value: Double): Double
}

data class RoundingBatchedCutoffReservationAdjuster(var step: Double) : BatchedCutoffReservationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedCutoffReservation, value: Double): Double = roundTo(value, step)
}

data class FlaggedBatchedCutoffReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedCutoffReservationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedCutoffReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredBatchedCutoffReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedCutoffReservationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedCutoffReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ChannelBatchedCutoffReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedCutoffReservationAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: BatchedCutoffReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BatchedCutoffReservationFuelLitersOffsetAdjuster(
    var weight: Double,
) : BatchedCutoffReservationAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: BatchedCutoffReservation, value: Double): Double = value + item.fuelLiters * weight
}

/** Runs the batched cutoff reservation steps in order and keeps a trace of each. */
data class BatchedCutoffReservationEngine(
    var steps: MutableList<BatchedCutoffReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedCutoffReservation): Double {
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

    fun evaluateAll(items: MutableList<BatchedCutoffReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedCutoffReservationEngine(flags: Registry): BatchedCutoffReservationEngine {
    return BatchedCutoffReservationEngine(
        steps = mutableListOf<BatchedCutoffReservationAdjuster>(
            RoundingBatchedCutoffReservationAdjuster(5.0),
            FlaggedBatchedCutoffReservationAdjuster(flags, "batched-cutoff-reservation-regional", -0.1),
            TieredBatchedCutoffReservationAdjuster(
                mutableListOf<Double>(1576.0, 4951.0),
                mutableListOf<Double>(1.19, 0.81),
            ),
            ChannelBatchedCutoffReservationAdjuster(mutableMapOf<String, Double>("north" to 1.13)),
            BatchedCutoffReservationFuelLitersOffsetAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 49680.6,
    )
}
