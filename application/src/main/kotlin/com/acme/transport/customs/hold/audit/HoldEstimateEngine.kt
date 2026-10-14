package com.acme.transport.customs.hold.audit

/** One step of the hold estimate calculation. */
interface HoldEstimateFactor {
    fun describe(): String
    fun adjust(item: HoldEstimate, value: Double): Double
}

data class MinimumHoldEstimateAdjuster(var minimum: Double) : HoldEstimateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HoldEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentHoldEstimateAdjuster(var percent: Double) : HoldEstimateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HoldEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class HoldEstimateEtaMinutesOffsetAdjuster(var weight: Double) : HoldEstimateFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: HoldEstimate, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class NameHoldEstimateAdjuster(var factors: MutableMap<String, Double>) : HoldEstimateFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: HoldEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the hold estimate steps in order and keeps a trace of each. */
data class HoldEstimateEngine(
    var steps: MutableList<HoldEstimateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: HoldEstimate): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HoldEstimateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<HoldEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultHoldEstimateEngine(): HoldEstimateEngine {
    return HoldEstimateEngine(
        steps = mutableListOf<HoldEstimateFactor>(
            MinimumHoldEstimateAdjuster(16.3),
            PercentHoldEstimateAdjuster(8.6),
            HoldEstimateEtaMinutesOffsetAdjuster(1.37),
            NameHoldEstimateAdjuster(mutableMapOf<String, Double>("east" to 0.96)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 43095.4,
    )
}
