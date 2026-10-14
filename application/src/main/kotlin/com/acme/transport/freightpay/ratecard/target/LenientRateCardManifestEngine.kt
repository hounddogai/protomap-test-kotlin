package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient rate card manifest calculation. */
interface LenientRateCardManifestStep {
    fun describe(): String
    fun adjust(item: LenientRateCardManifest, value: Double): Double
}

data class CapLenientRateCardManifestAdjuster(var cap: Double) : LenientRateCardManifestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientRateCardManifest, value: Double): Double = minOf(value, cap)
}

data class TieredLenientRateCardManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientRateCardManifestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientRateCardManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLenientRateCardManifestAdjuster(var percent: Double) : LenientRateCardManifestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientRateCardManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ChannelLenientRateCardManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientRateCardManifestStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: LenientRateCardManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient rate card manifest steps in order and keeps a trace of each. */
data class LenientRateCardManifestEngine(
    var steps: MutableList<LenientRateCardManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientRateCardManifest): Double {
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

    fun evaluateAll(items: MutableList<LenientRateCardManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientRateCardManifestEngine(): LenientRateCardManifestEngine {
    return LenientRateCardManifestEngine(
        steps = mutableListOf<LenientRateCardManifestStep>(
            CapLenientRateCardManifestAdjuster(30992.0),
            TieredLenientRateCardManifestAdjuster(
                mutableListOf<Double>(2982.0, 5374.0, 5475.0, 6864.0),
                mutableListOf<Double>(0.88, 0.95, 1.01, 1.18),
            ),
            PercentLenientRateCardManifestAdjuster(-19.1),
            ChannelLenientRateCardManifestAdjuster(
                mutableMapOf<String, Double>("south" to 1.35, "west" to 1.28, "online" to 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 18881.1,
    )
}
