package com.acme.transport.lastmile.parcel.profile

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary parcel draft calculation. */
interface PrimaryParcelDraftAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryParcelDraft, value: Double): Double
}

data class PrimaryParcelDraftThresholdOffsetAdjuster(var weight: Double) : PrimaryParcelDraftAdjuster {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: PrimaryParcelDraft, value: Double): Double = value + item.threshold * weight
}

data class RoundingPrimaryParcelDraftAdjuster(var step: Double) : PrimaryParcelDraftAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryParcelDraft, value: Double): Double = roundTo(value, step)
}

data class TieredPrimaryParcelDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryParcelDraftAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryParcelDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary parcel draft steps in order and keeps a trace of each. */
data class PrimaryParcelDraftEngine(
    var steps: MutableList<PrimaryParcelDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryParcelDraft): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryParcelDraftAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryParcelDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryParcelDraftEngine(): PrimaryParcelDraftEngine {
    return PrimaryParcelDraftEngine(
        steps = mutableListOf<PrimaryParcelDraftAdjuster>(
            PrimaryParcelDraftThresholdOffsetAdjuster(1.1),
            RoundingPrimaryParcelDraftAdjuster(1.0),
            TieredPrimaryParcelDraftAdjuster(
                mutableListOf<Double>(2231.0, 9073.0, 9092.0),
                mutableListOf<Double>(1.12, 0.97, 0.81),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 10589.0,
    )
}
