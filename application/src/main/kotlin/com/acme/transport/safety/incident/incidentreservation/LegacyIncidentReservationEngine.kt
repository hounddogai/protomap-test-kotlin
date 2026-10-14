package com.acme.transport.safety.incident.incidentreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy incident reservation calculation. */
interface LegacyIncidentReservationModifier {
    fun describe(): String
    fun adjust(item: LegacyIncidentReservation, value: Double): Double
}

data class LegacyIncidentReservationGraceDaysOffsetAdjuster(
    var weight: Double,
) : LegacyIncidentReservationModifier {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: LegacyIncidentReservation, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class TitleLegacyIncidentReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyIncidentReservationModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: LegacyIncidentReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLegacyIncidentReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyIncidentReservationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyIncidentReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLegacyIncidentReservationAdjuster(var percent: Double) : LegacyIncidentReservationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyIncidentReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLegacyIncidentReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyIncidentReservationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyIncidentReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy incident reservation steps in order and keeps a trace of each. */
data class LegacyIncidentReservationEngine(
    var steps: MutableList<LegacyIncidentReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyIncidentReservation): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyIncidentReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyIncidentReservationEngine(flags: Registry): LegacyIncidentReservationEngine {
    return LegacyIncidentReservationEngine(
        steps = mutableListOf<LegacyIncidentReservationModifier>(
            LegacyIncidentReservationGraceDaysOffsetAdjuster(1.44),
            TitleLegacyIncidentReservationAdjuster(
                mutableMapOf<String, Double>("west" to 1.17, "east" to 0.97, "north" to 0.92),
            ),
            FlaggedLegacyIncidentReservationAdjuster(flags, "legacy-incident-reservation-manual", -2.6),
            PercentLegacyIncidentReservationAdjuster(14.9),
            TieredLegacyIncidentReservationAdjuster(
                mutableListOf<Double>(1075.0, 3896.0, 6568.0, 6851.0),
                mutableListOf<Double>(0.92, 1.16, 0.8, 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 98441.2,
    )
}
