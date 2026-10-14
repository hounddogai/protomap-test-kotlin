package com.acme.transport.network.hub.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the hub score calculation. */
interface HubScoreFactor {
    fun describe(): String
    fun adjust(item: HubScore, value: Double): Double
}

data class HubScoreThresholdOffsetAdjuster(var weight: Double) : HubScoreFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: HubScore, value: Double): Double = value + item.threshold * weight
}

data class PercentHubScoreAdjuster(var percent: Double) : HubScoreFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HubScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapHubScoreAdjuster(var cap: Double) : HubScoreFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HubScore, value: Double): Double = minOf(value, cap)
}

data class RoundingHubScoreAdjuster(var step: Double) : HubScoreFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HubScore, value: Double): Double = roundTo(value, step)
}

data class FlaggedHubScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HubScoreFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HubScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredHubScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HubScoreFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HubScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the hub score steps in order and keeps a trace of each. */
data class HubScoreEngine(
    var steps: MutableList<HubScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: HubScore): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HubScoreFactor = steps[index]
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

    fun evaluateAll(items: MutableList<HubScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultHubScoreEngine(flags: Registry): HubScoreEngine {
    return HubScoreEngine(
        steps = mutableListOf<HubScoreFactor>(
            HubScoreThresholdOffsetAdjuster(1.7),
            PercentHubScoreAdjuster(-7.4),
            CapHubScoreAdjuster(27381.0),
            RoundingHubScoreAdjuster(0.05),
            FlaggedHubScoreAdjuster(flags, "hub-score-manual", 3.3),
            TieredHubScoreAdjuster(mutableListOf<Double>(2665.0, 8702.0), mutableListOf<Double>(0.84, 1.11)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 76470.9,
    )
}
