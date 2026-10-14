package com.acme.transport.terminal.appointment.appointmentscore

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered appointment claim calculation. */
interface TieredAppointmentClaimModifier {
    fun describe(): String
    fun adjust(item: TieredAppointmentClaim, value: Double): Double
}

data class TieredAppointmentClaimVersionOffsetAdjuster(var weight: Double) : TieredAppointmentClaimModifier {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: TieredAppointmentClaim, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class CategoryTieredAppointmentClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredAppointmentClaimModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: TieredAppointmentClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentTieredAppointmentClaimAdjuster(var percent: Double) : TieredAppointmentClaimModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredAppointmentClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tiered appointment claim steps in order and keeps a trace of each. */
data class TieredAppointmentClaimEngine(
    var steps: MutableList<TieredAppointmentClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredAppointmentClaim): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredAppointmentClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredAppointmentClaimEngine(): TieredAppointmentClaimEngine {
    return TieredAppointmentClaimEngine(
        steps = mutableListOf<TieredAppointmentClaimModifier>(
            TieredAppointmentClaimVersionOffsetAdjuster(0.14),
            CategoryTieredAppointmentClaimAdjuster(
                mutableMapOf<String, Double>("central" to 0.85, "retail" to 0.89, "east" to 1.16),
            ),
            PercentTieredAppointmentClaimAdjuster(-9.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 90349.8,
    )
}
