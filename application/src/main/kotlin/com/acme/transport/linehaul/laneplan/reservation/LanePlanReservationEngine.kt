package com.acme.transport.linehaul.laneplan.reservation

import com.acme.transport.kit.flags.Registry

/** One step of the lane plan reservation calculation. */
interface LanePlanReservationAdjuster {
    fun describe(): String
    fun adjust(item: LanePlanReservation, value: Double): Double
}

data class MinimumLanePlanReservationAdjuster(var minimum: Double) : LanePlanReservationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LanePlanReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLanePlanReservationAdjuster(var percent: Double) : LanePlanReservationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LanePlanReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLanePlanReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LanePlanReservationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LanePlanReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lane plan reservation steps in order and keeps a trace of each. */
data class LanePlanReservationEngine(
    var steps: MutableList<LanePlanReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LanePlanReservation): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LanePlanReservationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LanePlanReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLanePlanReservationEngine(flags: Registry): LanePlanReservationEngine {
    return LanePlanReservationEngine(
        steps = mutableListOf<LanePlanReservationAdjuster>(
            MinimumLanePlanReservationAdjuster(22.6),
            PercentLanePlanReservationAdjuster(-2.8),
            FlaggedLanePlanReservationAdjuster(flags, "lane-plan-reservation-default", 1.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 44260.2,
    )
}
