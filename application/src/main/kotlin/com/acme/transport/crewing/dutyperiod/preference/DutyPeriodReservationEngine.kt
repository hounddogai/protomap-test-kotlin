package com.acme.transport.crewing.dutyperiod.preference

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the duty period reservation calculation. */
interface DutyPeriodReservationModifier {
    fun describe(): String
    fun adjust(item: DutyPeriodReservation, value: Double): Double
}

data class CapDutyPeriodReservationAdjuster(var cap: Double) : DutyPeriodReservationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DutyPeriodReservation, value: Double): Double = minOf(value, cap)
}

data class PercentDutyPeriodReservationAdjuster(var percent: Double) : DutyPeriodReservationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DutyPeriodReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDutyPeriodReservationAdjuster(var step: Double) : DutyPeriodReservationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DutyPeriodReservation, value: Double): Double = roundTo(value, step)
}

/** Runs the duty period reservation steps in order and keeps a trace of each. */
data class DutyPeriodReservationEngine(
    var steps: MutableList<DutyPeriodReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DutyPeriodReservation): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DutyPeriodReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDutyPeriodReservationEngine(): DutyPeriodReservationEngine {
    return DutyPeriodReservationEngine(
        steps = mutableListOf<DutyPeriodReservationModifier>(
            CapDutyPeriodReservationAdjuster(38002.0),
            PercentDutyPeriodReservationAdjuster(-15.2),
            RoundingDutyPeriodReservationAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 49046.1,
    )
}
