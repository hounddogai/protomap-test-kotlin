package com.acme.transport.emissions.carbonreport.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the carbon report reservation calculation. */
interface CarbonReportReservationFactor {
    fun describe(): String
    fun adjust(item: CarbonReportReservation, value: Double): Double
}

data class RoundingCarbonReportReservationAdjuster(var step: Double) : CarbonReportReservationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CarbonReportReservation, value: Double): Double = roundTo(value, step)
}

data class FlaggedCarbonReportReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CarbonReportReservationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CarbonReportReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ChannelCarbonReportReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : CarbonReportReservationFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: CarbonReportReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CarbonReportReservationRetriesOffsetAdjuster(var weight: Double) : CarbonReportReservationFactor {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: CarbonReportReservation, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

/** Runs the carbon report reservation steps in order and keeps a trace of each. */
data class CarbonReportReservationEngine(
    var steps: MutableList<CarbonReportReservationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CarbonReportReservation): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CarbonReportReservationFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CarbonReportReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCarbonReportReservationEngine(flags: Registry): CarbonReportReservationEngine {
    return CarbonReportReservationEngine(
        steps = mutableListOf<CarbonReportReservationFactor>(
            RoundingCarbonReportReservationAdjuster(0.25),
            FlaggedCarbonReportReservationAdjuster(flags, "carbon-report-reservation-adaptive", -2.7),
            ChannelCarbonReportReservationAdjuster(mutableMapOf<String, Double>("south" to 0.86)),
            CarbonReportReservationRetriesOffsetAdjuster(0.37),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 76091.6,
    )
}
