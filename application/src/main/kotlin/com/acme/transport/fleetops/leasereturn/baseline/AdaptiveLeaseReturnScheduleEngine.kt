package com.acme.transport.fleetops.leasereturn.baseline

/** One step of the adaptive lease return schedule calculation. */
interface AdaptiveLeaseReturnScheduleFactor {
    fun describe(): String
    fun adjust(item: AdaptiveLeaseReturnSchedule, value: Double): Double
}

data class PercentAdaptiveLeaseReturnScheduleAdjuster(var percent: Double) : AdaptiveLeaseReturnScheduleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveLeaseReturnSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapAdaptiveLeaseReturnScheduleAdjuster(var cap: Double) : AdaptiveLeaseReturnScheduleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveLeaseReturnSchedule, value: Double): Double = minOf(value, cap)
}

data class AdaptiveLeaseReturnScheduleStopCountOffsetAdjuster(
    var weight: Double,
) : AdaptiveLeaseReturnScheduleFactor {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: AdaptiveLeaseReturnSchedule, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

/** Runs the adaptive lease return schedule steps in order and keeps a trace of each. */
data class AdaptiveLeaseReturnScheduleEngine(
    var steps: MutableList<AdaptiveLeaseReturnScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveLeaseReturnSchedule): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveLeaseReturnScheduleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveLeaseReturnSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveLeaseReturnScheduleEngine(): AdaptiveLeaseReturnScheduleEngine {
    return AdaptiveLeaseReturnScheduleEngine(
        steps = mutableListOf<AdaptiveLeaseReturnScheduleFactor>(
            PercentAdaptiveLeaseReturnScheduleAdjuster(1.3),
            CapAdaptiveLeaseReturnScheduleAdjuster(24270.0),
            AdaptiveLeaseReturnScheduleStopCountOffsetAdjuster(1.71),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 52927.2,
    )
}
