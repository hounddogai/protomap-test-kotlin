package com.acme.transport.linehaul.capacity.milestone

/** One step of the lenient capacity reservation calculation. */
interface LenientCapacityReservationAdjuster {
    fun describe(): String
    fun adjust(item: LenientCapacityReservation, value: Double): Double
}

data class CapLenientCapacityReservationAdjuster(var cap: Double) : LenientCapacityReservationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientCapacityReservation, value: Double): Double = minOf(value, cap)
}

data class MinimumLenientCapacityReservationAdjuster(var minimum: Double) : LenientCapacityReservationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientCapacityReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLenientCapacityReservationAdjuster(var percent: Double) : LenientCapacityReservationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientCapacityReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LenientCapacityReservationBatchSizeOffsetAdjuster(
    var weight: Double,
) : LenientCapacityReservationAdjuster {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: LenientCapacityReservation, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class DepotLenientCapacityReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientCapacityReservationAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: LenientCapacityReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient capacity reservation steps in order and keeps a trace of each. */
data class LenientCapacityReservationEngine(
    var steps: MutableList<LenientCapacityReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientCapacityReservation): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientCapacityReservationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LenientCapacityReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientCapacityReservationEngine(): LenientCapacityReservationEngine {
    return LenientCapacityReservationEngine(
        steps = mutableListOf<LenientCapacityReservationAdjuster>(
            CapLenientCapacityReservationAdjuster(20808.0),
            MinimumLenientCapacityReservationAdjuster(45.6),
            PercentLenientCapacityReservationAdjuster(8.5),
            LenientCapacityReservationBatchSizeOffsetAdjuster(1.95),
            DepotLenientCapacityReservationAdjuster(
                mutableMapOf<String, Double>("west" to 1.24, "online" to 1.29),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 57653.7,
    )
}
