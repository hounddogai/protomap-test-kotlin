package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default sortation reservation calculation. */
interface DefaultSortationReservationStep {
    fun describe(): String
    fun adjust(item: DefaultSortationReservation, value: Double): Double
}

data class RoundingDefaultSortationReservationAdjuster(var step: Double) : DefaultSortationReservationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultSortationReservation, value: Double): Double = roundTo(value, step)
}

data class DefaultSortationReservationWeightOffsetAdjuster(
    var weight: Double,
) : DefaultSortationReservationStep {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: DefaultSortationReservation, value: Double): Double = value + item.weight * weight
}

data class MinimumDefaultSortationReservationAdjuster(var minimum: Double) : DefaultSortationReservationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultSortationReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDefaultSortationReservationAdjuster(var percent: Double) : DefaultSortationReservationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultSortationReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDefaultSortationReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultSortationReservationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultSortationReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleDefaultSortationReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultSortationReservationStep {
    override fun describe(): String = "by title"

    override fun adjust(item: DefaultSortationReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the default sortation reservation steps in order and keeps a trace of each. */
data class DefaultSortationReservationEngine(
    var steps: MutableList<DefaultSortationReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultSortationReservation): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultSortationReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultSortationReservationEngine(flags: Registry): DefaultSortationReservationEngine {
    return DefaultSortationReservationEngine(
        steps = mutableListOf<DefaultSortationReservationStep>(
            RoundingDefaultSortationReservationAdjuster(1.0),
            DefaultSortationReservationWeightOffsetAdjuster(1.41),
            MinimumDefaultSortationReservationAdjuster(9.7),
            PercentDefaultSortationReservationAdjuster(8.3),
            FlaggedDefaultSortationReservationAdjuster(flags, "default-sortation-reservation-seasonal", 2.8),
            TitleDefaultSortationReservationAdjuster(
                mutableMapOf<String, Double>("central" to 0.7, "west" to 0.93),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 23887.1,
    )
}
