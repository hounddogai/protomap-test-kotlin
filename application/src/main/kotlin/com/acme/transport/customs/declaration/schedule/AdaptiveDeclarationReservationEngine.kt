package com.acme.transport.customs.declaration.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive declaration reservation calculation. */
interface AdaptiveDeclarationReservationAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveDeclarationReservation, value: Double): Double
}

data class RoundingAdaptiveDeclarationReservationAdjuster(
    var step: Double,
) : AdaptiveDeclarationReservationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveDeclarationReservation, value: Double): Double = roundTo(value, step)
}

data class MinimumAdaptiveDeclarationReservationAdjuster(
    var minimum: Double,
) : AdaptiveDeclarationReservationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveDeclarationReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAdaptiveDeclarationReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveDeclarationReservationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveDeclarationReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionAdaptiveDeclarationReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveDeclarationReservationAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: AdaptiveDeclarationReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive declaration reservation steps in order and keeps a trace of each. */
data class AdaptiveDeclarationReservationEngine(
    var steps: MutableList<AdaptiveDeclarationReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveDeclarationReservation): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveDeclarationReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveDeclarationReservationEngine(flags: Registry): AdaptiveDeclarationReservationEngine {
    return AdaptiveDeclarationReservationEngine(
        steps = mutableListOf<AdaptiveDeclarationReservationAdjuster>(
            RoundingAdaptiveDeclarationReservationAdjuster(0.25),
            MinimumAdaptiveDeclarationReservationAdjuster(43.4),
            FlaggedAdaptiveDeclarationReservationAdjuster(
                flags,
                "adaptive-declaration-reservation-lenient",
                -4.8,
            ),
            RegionAdaptiveDeclarationReservationAdjuster(mutableMapOf<String, Double>("south" to 0.89)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 24845.8,
    )
}
