package com.acme.transport.terminal.traileryard.yardreservation

/** One step of the batched trailer yard reservation calculation. */
interface BatchedTrailerYardReservationAdjuster {
    fun describe(): String
    fun adjust(item: BatchedTrailerYardReservation, value: Double): Double
}

data class ChannelBatchedTrailerYardReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedTrailerYardReservationAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: BatchedTrailerYardReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumBatchedTrailerYardReservationAdjuster(
    var minimum: Double,
) : BatchedTrailerYardReservationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedTrailerYardReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class BatchedTrailerYardReservationCapacityOffsetAdjuster(
    var weight: Double,
) : BatchedTrailerYardReservationAdjuster {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: BatchedTrailerYardReservation, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class PercentBatchedTrailerYardReservationAdjuster(
    var percent: Double,
) : BatchedTrailerYardReservationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedTrailerYardReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredBatchedTrailerYardReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedTrailerYardReservationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedTrailerYardReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the batched trailer yard reservation steps in order and keeps a trace of each. */
data class BatchedTrailerYardReservationEngine(
    var steps: MutableList<BatchedTrailerYardReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedTrailerYardReservation): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedTrailerYardReservationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedTrailerYardReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedTrailerYardReservationEngine(): BatchedTrailerYardReservationEngine {
    return BatchedTrailerYardReservationEngine(
        steps = mutableListOf<BatchedTrailerYardReservationAdjuster>(
            ChannelBatchedTrailerYardReservationAdjuster(
                mutableMapOf<String, Double>("east" to 0.96, "north" to 1.16),
            ),
            MinimumBatchedTrailerYardReservationAdjuster(37.4),
            BatchedTrailerYardReservationCapacityOffsetAdjuster(0.42),
            PercentBatchedTrailerYardReservationAdjuster(-7.6),
            TieredBatchedTrailerYardReservationAdjuster(
                mutableListOf<Double>(7935.0, 8326.0),
                mutableListOf<Double>(1.07, 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 79337.4,
    )
}
