package com.acme.transport.customs.declaration.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback declaration reservation calculation. */
interface FallbackDeclarationReservationFactor {
    fun describe(): String
    fun adjust(item: FallbackDeclarationReservation, value: Double): Double
}

data class MinimumFallbackDeclarationReservationAdjuster(
    var minimum: Double,
) : FallbackDeclarationReservationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackDeclarationReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FallbackDeclarationReservationEtaMinutesOffsetAdjuster(
    var weight: Double,
) : FallbackDeclarationReservationFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: FallbackDeclarationReservation, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class CapFallbackDeclarationReservationAdjuster(var cap: Double) : FallbackDeclarationReservationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackDeclarationReservation, value: Double): Double = minOf(value, cap)
}

data class FlaggedFallbackDeclarationReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackDeclarationReservationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackDeclarationReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentFallbackDeclarationReservationAdjuster(
    var percent: Double,
) : FallbackDeclarationReservationFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackDeclarationReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameFallbackDeclarationReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackDeclarationReservationFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: FallbackDeclarationReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fallback declaration reservation steps in order and keeps a trace of each. */
data class FallbackDeclarationReservationEngine(
    var steps: MutableList<FallbackDeclarationReservationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackDeclarationReservation): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackDeclarationReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackDeclarationReservationEngine(flags: Registry): FallbackDeclarationReservationEngine {
    return FallbackDeclarationReservationEngine(
        steps = mutableListOf<FallbackDeclarationReservationFactor>(
            MinimumFallbackDeclarationReservationAdjuster(15.8),
            FallbackDeclarationReservationEtaMinutesOffsetAdjuster(0.08),
            CapFallbackDeclarationReservationAdjuster(8582.0),
            FlaggedFallbackDeclarationReservationAdjuster(
                flags,
                "fallback-declaration-reservation-rolling",
                -9.7,
            ),
            PercentFallbackDeclarationReservationAdjuster(-13.5),
            NameFallbackDeclarationReservationAdjuster(mutableMapOf<String, Double>("central" to 1.35)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 29116.2,
    )
}
