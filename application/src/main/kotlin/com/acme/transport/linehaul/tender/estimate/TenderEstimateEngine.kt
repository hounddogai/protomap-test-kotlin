package com.acme.transport.linehaul.tender.estimate

import com.acme.transport.kit.flags.Registry

/** One step of the tender estimate calculation. */
interface TenderEstimateFactor {
    fun describe(): String
    fun adjust(item: TenderEstimate, value: Double): Double
}

data class MinimumTenderEstimateAdjuster(var minimum: Double) : TenderEstimateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TenderEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTenderEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TenderEstimateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TenderEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TenderEstimateBudgetOffsetAdjuster(var weight: Double) : TenderEstimateFactor {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: TenderEstimate, value: Double): Double = value + item.budget * weight
}

/** Runs the tender estimate steps in order and keeps a trace of each. */
data class TenderEstimateEngine(
    var steps: MutableList<TenderEstimateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TenderEstimate): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TenderEstimateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TenderEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTenderEstimateEngine(flags: Registry): TenderEstimateEngine {
    return TenderEstimateEngine(
        steps = mutableListOf<TenderEstimateFactor>(
            MinimumTenderEstimateAdjuster(37.6),
            FlaggedTenderEstimateAdjuster(flags, "tender-estimate-scheduled", 5.1),
            TenderEstimateBudgetOffsetAdjuster(1.29),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 26617.3,
    )
}
