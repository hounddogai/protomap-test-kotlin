package com.acme.transport.fleetops.tire.recorditem

/** One step of the strict tire record calculation. */
interface StrictTireRecordAdjuster {
    fun describe(): String
    fun adjust(item: StrictTireRecord, value: Double): Double
}

data class PercentStrictTireRecordAdjuster(var percent: Double) : StrictTireRecordAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictTireRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class StrictTireRecordBudgetOffsetAdjuster(var weight: Double) : StrictTireRecordAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: StrictTireRecord, value: Double): Double = value + item.budget * weight
}

data class CapStrictTireRecordAdjuster(var cap: Double) : StrictTireRecordAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictTireRecord, value: Double): Double = minOf(value, cap)
}

data class TieredStrictTireRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictTireRecordAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictTireRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelStrictTireRecordAdjuster(var factors: MutableMap<String, Double>) : StrictTireRecordAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: StrictTireRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict tire record steps in order and keeps a trace of each. */
data class StrictTireRecordEngine(
    var steps: MutableList<StrictTireRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictTireRecord): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictTireRecordAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictTireRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictTireRecordEngine(): StrictTireRecordEngine {
    return StrictTireRecordEngine(
        steps = mutableListOf<StrictTireRecordAdjuster>(
            PercentStrictTireRecordAdjuster(23.4),
            StrictTireRecordBudgetOffsetAdjuster(1.08),
            CapStrictTireRecordAdjuster(49837.0),
            TieredStrictTireRecordAdjuster(
                mutableListOf<Double>(679.0, 5682.0, 6969.0),
                mutableListOf<Double>(1.03, 0.98, 0.97),
            ),
            LabelStrictTireRecordAdjuster(mutableMapOf<String, Double>("south" to 0.79, "west" to 0.96)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 94371.5,
    )
}
