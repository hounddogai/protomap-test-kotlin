package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered proof of delivery score calculation. */
interface TieredProofOfDeliveryScoreAdjuster {
    fun describe(): String
    fun adjust(item: TieredProofOfDeliveryScore, value: Double): Double
}

data class LaneTieredProofOfDeliveryScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredProofOfDeliveryScoreAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: TieredProofOfDeliveryScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentTieredProofOfDeliveryScoreAdjuster(var percent: Double) : TieredProofOfDeliveryScoreAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredProofOfDeliveryScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredTieredProofOfDeliveryScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredProofOfDeliveryScoreAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredProofOfDeliveryScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumTieredProofOfDeliveryScoreAdjuster(var minimum: Double) : TieredProofOfDeliveryScoreAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredProofOfDeliveryScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapTieredProofOfDeliveryScoreAdjuster(var cap: Double) : TieredProofOfDeliveryScoreAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredProofOfDeliveryScore, value: Double): Double = minOf(value, cap)
}

/** Runs the tiered proof of delivery score steps in order and keeps a trace of each. */
data class TieredProofOfDeliveryScoreEngine(
    var steps: MutableList<TieredProofOfDeliveryScoreAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredProofOfDeliveryScore): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredProofOfDeliveryScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredProofOfDeliveryScoreEngine(): TieredProofOfDeliveryScoreEngine {
    return TieredProofOfDeliveryScoreEngine(
        steps = mutableListOf<TieredProofOfDeliveryScoreAdjuster>(
            LaneTieredProofOfDeliveryScoreAdjuster(
                mutableMapOf<String, Double>("north" to 1.04, "south" to 1.27, "online" to 1.22),
            ),
            PercentTieredProofOfDeliveryScoreAdjuster(25.0),
            TieredTieredProofOfDeliveryScoreAdjuster(
                mutableListOf<Double>(2035.0, 3831.0),
                mutableListOf<Double>(1.06, 0.88),
            ),
            MinimumTieredProofOfDeliveryScoreAdjuster(27.3),
            CapTieredProofOfDeliveryScoreAdjuster(33953.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 19565.0,
    )
}
