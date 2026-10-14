package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default proof of delivery score calculation. */
interface DefaultProofOfDeliveryScoreFactor {
    fun describe(): String
    fun adjust(item: DefaultProofOfDeliveryScore, value: Double): Double
}

data class MinimumDefaultProofOfDeliveryScoreAdjuster(var minimum: Double) : DefaultProofOfDeliveryScoreFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultProofOfDeliveryScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredDefaultProofOfDeliveryScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultProofOfDeliveryScoreFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultProofOfDeliveryScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDefaultProofOfDeliveryScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultProofOfDeliveryScoreFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultProofOfDeliveryScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the default proof of delivery score steps in order and keeps a trace of each. */
data class DefaultProofOfDeliveryScoreEngine(
    var steps: MutableList<DefaultProofOfDeliveryScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultProofOfDeliveryScore): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultProofOfDeliveryScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultProofOfDeliveryScoreEngine(flags: Registry): DefaultProofOfDeliveryScoreEngine {
    return DefaultProofOfDeliveryScoreEngine(
        steps = mutableListOf<DefaultProofOfDeliveryScoreFactor>(
            MinimumDefaultProofOfDeliveryScoreAdjuster(3.2),
            TieredDefaultProofOfDeliveryScoreAdjuster(
                mutableListOf<Double>(1663.0, 4690.0),
                mutableListOf<Double>(1.0, 1.03),
            ),
            FlaggedDefaultProofOfDeliveryScoreAdjuster(flags, "default-proof-of-delivery-score-manual", 0.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 22662.0,
    )
}
