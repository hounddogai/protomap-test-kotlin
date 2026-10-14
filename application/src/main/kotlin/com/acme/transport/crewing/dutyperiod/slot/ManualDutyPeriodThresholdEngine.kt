package com.acme.transport.crewing.dutyperiod.slot

/** One step of the manual duty period threshold calculation. */
interface ManualDutyPeriodThresholdModifier {
    fun describe(): String
    fun adjust(item: ManualDutyPeriodThreshold, value: Double): Double
}

data class MinimumManualDutyPeriodThresholdAdjuster(var minimum: Double) : ManualDutyPeriodThresholdModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualDutyPeriodThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ManualDutyPeriodThresholdDistanceKmOffsetAdjuster(
    var weight: Double,
) : ManualDutyPeriodThresholdModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: ManualDutyPeriodThreshold, value: Double): Double = value + item.distanceKm * weight
}

data class CapManualDutyPeriodThresholdAdjuster(var cap: Double) : ManualDutyPeriodThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualDutyPeriodThreshold, value: Double): Double = minOf(value, cap)
}

data class TieredManualDutyPeriodThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualDutyPeriodThresholdModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualDutyPeriodThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentManualDutyPeriodThresholdAdjuster(var percent: Double) : ManualDutyPeriodThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualDutyPeriodThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the manual duty period threshold steps in order and keeps a trace of each. */
data class ManualDutyPeriodThresholdEngine(
    var steps: MutableList<ManualDutyPeriodThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualDutyPeriodThreshold): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualDutyPeriodThresholdModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ManualDutyPeriodThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualDutyPeriodThresholdEngine(): ManualDutyPeriodThresholdEngine {
    return ManualDutyPeriodThresholdEngine(
        steps = mutableListOf<ManualDutyPeriodThresholdModifier>(
            MinimumManualDutyPeriodThresholdAdjuster(45.1),
            ManualDutyPeriodThresholdDistanceKmOffsetAdjuster(1.52),
            CapManualDutyPeriodThresholdAdjuster(44992.0),
            TieredManualDutyPeriodThresholdAdjuster(
                mutableListOf<Double>(5505.0, 9674.0),
                mutableListOf<Double>(0.86, 0.9),
            ),
            PercentManualDutyPeriodThresholdAdjuster(-3.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 92196.2,
    )
}
