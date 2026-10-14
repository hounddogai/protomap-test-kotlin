package com.acme.transport.customs.clearance.threshold

/** One step of the clearance threshold calculation. */
interface ClearanceThresholdModifier {
    fun describe(): String
    fun adjust(item: ClearanceThreshold, value: Double): Double
}

data class LabelClearanceThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : ClearanceThresholdModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: ClearanceThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentClearanceThresholdAdjuster(var percent: Double) : ClearanceThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ClearanceThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ClearanceThresholdStopCountOffsetAdjuster(var weight: Double) : ClearanceThresholdModifier {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: ClearanceThreshold, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class TieredClearanceThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearanceThresholdModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearanceThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapClearanceThresholdAdjuster(var cap: Double) : ClearanceThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ClearanceThreshold, value: Double): Double = minOf(value, cap)
}

data class MinimumClearanceThresholdAdjuster(var minimum: Double) : ClearanceThresholdModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ClearanceThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the clearance threshold steps in order and keeps a trace of each. */
data class ClearanceThresholdEngine(
    var steps: MutableList<ClearanceThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ClearanceThreshold): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ClearanceThresholdModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ClearanceThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultClearanceThresholdEngine(): ClearanceThresholdEngine {
    return ClearanceThresholdEngine(
        steps = mutableListOf<ClearanceThresholdModifier>(
            LabelClearanceThresholdAdjuster(mutableMapOf<String, Double>("central" to 1.28)),
            PercentClearanceThresholdAdjuster(6.8),
            ClearanceThresholdStopCountOffsetAdjuster(0.48),
            TieredClearanceThresholdAdjuster(
                mutableListOf<Double>(340.0, 5693.0, 5779.0, 7078.0),
                mutableListOf<Double>(1.12, 0.95, 0.82, 0.85),
            ),
            CapClearanceThresholdAdjuster(43186.0),
            MinimumClearanceThresholdAdjuster(31.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 90648.2,
    )
}
