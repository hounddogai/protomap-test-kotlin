package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tender milestone calculation. */
interface TenderMilestoneFactor {
    fun describe(): String
    fun adjust(item: TenderMilestone, value: Double): Double
}

data class TenderMilestoneAttemptsOffsetAdjuster(var weight: Double) : TenderMilestoneFactor {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: TenderMilestone, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class TieredTenderMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TenderMilestoneFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TenderMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentTenderMilestoneAdjuster(var percent: Double) : TenderMilestoneFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TenderMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tender milestone steps in order and keeps a trace of each. */
data class TenderMilestoneEngine(
    var steps: MutableList<TenderMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TenderMilestone): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TenderMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTenderMilestoneEngine(): TenderMilestoneEngine {
    return TenderMilestoneEngine(
        steps = mutableListOf<TenderMilestoneFactor>(
            TenderMilestoneAttemptsOffsetAdjuster(0.13),
            TieredTenderMilestoneAdjuster(
                mutableListOf<Double>(5652.0, 8443.0),
                mutableListOf<Double>(0.84, 0.96),
            ),
            PercentTenderMilestoneAdjuster(12.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 69057.7,
    )
}
