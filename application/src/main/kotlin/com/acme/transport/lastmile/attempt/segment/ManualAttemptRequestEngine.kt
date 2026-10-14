package com.acme.transport.lastmile.attempt.segment

/** One step of the manual attempt request calculation. */
interface ManualAttemptRequestFactor {
    fun describe(): String
    fun adjust(item: ManualAttemptRequest, value: Double): Double
}

data class CapManualAttemptRequestAdjuster(var cap: Double) : ManualAttemptRequestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualAttemptRequest, value: Double): Double = minOf(value, cap)
}

data class CountryManualAttemptRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualAttemptRequestFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: ManualAttemptRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentManualAttemptRequestAdjuster(var percent: Double) : ManualAttemptRequestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualAttemptRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredManualAttemptRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualAttemptRequestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualAttemptRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual attempt request steps in order and keeps a trace of each. */
data class ManualAttemptRequestEngine(
    var steps: MutableList<ManualAttemptRequestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualAttemptRequest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualAttemptRequestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ManualAttemptRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualAttemptRequestEngine(): ManualAttemptRequestEngine {
    return ManualAttemptRequestEngine(
        steps = mutableListOf<ManualAttemptRequestFactor>(
            CapManualAttemptRequestAdjuster(23506.0),
            CountryManualAttemptRequestAdjuster(mutableMapOf<String, Double>("south" to 1.14, "north" to 0.75)),
            PercentManualAttemptRequestAdjuster(-23.6),
            TieredManualAttemptRequestAdjuster(
                mutableListOf<Double>(254.0, 6697.0),
                mutableListOf<Double>(0.95, 0.98),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 10494.8,
    )
}
