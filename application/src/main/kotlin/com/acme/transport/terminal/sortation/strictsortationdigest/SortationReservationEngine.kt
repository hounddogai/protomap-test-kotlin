package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the sortation reservation calculation. */
interface SortationReservationStep {
    fun describe(): String
    fun adjust(item: SortationReservation, value: Double): Double
}

data class RoundingSortationReservationAdjuster(var step: Double) : SortationReservationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SortationReservation, value: Double): Double = roundTo(value, step)
}

data class ChannelSortationReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : SortationReservationStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: SortationReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumSortationReservationAdjuster(var minimum: Double) : SortationReservationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SortationReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSortationReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SortationReservationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SortationReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the sortation reservation steps in order and keeps a trace of each. */
data class SortationReservationEngine(
    var steps: MutableList<SortationReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SortationReservation): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SortationReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSortationReservationEngine(flags: Registry): SortationReservationEngine {
    return SortationReservationEngine(
        steps = mutableListOf<SortationReservationStep>(
            RoundingSortationReservationAdjuster(0.1),
            ChannelSortationReservationAdjuster(mutableMapOf<String, Double>("south" to 1.0, "north" to 1.35)),
            MinimumSortationReservationAdjuster(16.4),
            FlaggedSortationReservationAdjuster(flags, "sortation-reservation-default", 4.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 67684.7,
    )
}
