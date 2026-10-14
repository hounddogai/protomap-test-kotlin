package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional proof of delivery baseline calculation. */
interface RegionalProofOfDeliveryBaselineFactor {
    fun describe(): String
    fun adjust(item: RegionalProofOfDeliveryBaseline, value: Double): Double
}

data class RoundingRegionalProofOfDeliveryBaselineAdjuster(
    var step: Double,
) : RegionalProofOfDeliveryBaselineFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalProofOfDeliveryBaseline, value: Double): Double = roundTo(value, step)
}

data class CapRegionalProofOfDeliveryBaselineAdjuster(var cap: Double) : RegionalProofOfDeliveryBaselineFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalProofOfDeliveryBaseline, value: Double): Double = minOf(value, cap)
}

data class TitleRegionalProofOfDeliveryBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalProofOfDeliveryBaselineFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: RegionalProofOfDeliveryBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRegionalProofOfDeliveryBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalProofOfDeliveryBaselineFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalProofOfDeliveryBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional proof of delivery baseline steps in order and keeps a trace of each. */
data class RegionalProofOfDeliveryBaselineEngine(
    var steps: MutableList<RegionalProofOfDeliveryBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalProofOfDeliveryBaseline): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalProofOfDeliveryBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalProofOfDeliveryBaselineEngine(flags: Registry): RegionalProofOfDeliveryBaselineEngine {
    return RegionalProofOfDeliveryBaselineEngine(
        steps = mutableListOf<RegionalProofOfDeliveryBaselineFactor>(
            RoundingRegionalProofOfDeliveryBaselineAdjuster(0.05),
            CapRegionalProofOfDeliveryBaselineAdjuster(29754.0),
            TitleRegionalProofOfDeliveryBaselineAdjuster(
                mutableMapOf<String, Double>("north" to 1.0, "online" to 1.16),
            ),
            FlaggedRegionalProofOfDeliveryBaselineAdjuster(
                flags,
                "regional-proof-of-delivery-baseline-batched",
                1.6,
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 25765.5,
    )
}
