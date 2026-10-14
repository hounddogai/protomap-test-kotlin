package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered rate card manifest calculation. */
interface TieredRateCardManifestStep {
    fun describe(): String
    fun adjust(item: TieredRateCardManifest, value: Double): Double
}

data class PercentTieredRateCardManifestAdjuster(var percent: Double) : TieredRateCardManifestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredRateCardManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingTieredRateCardManifestAdjuster(var step: Double) : TieredRateCardManifestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredRateCardManifest, value: Double): Double = roundTo(value, step)
}

data class FlaggedTieredRateCardManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredRateCardManifestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredRateCardManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tiered rate card manifest steps in order and keeps a trace of each. */
data class TieredRateCardManifestEngine(
    var steps: MutableList<TieredRateCardManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredRateCardManifest): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredRateCardManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredRateCardManifestEngine(flags: Registry): TieredRateCardManifestEngine {
    return TieredRateCardManifestEngine(
        steps = mutableListOf<TieredRateCardManifestStep>(
            PercentTieredRateCardManifestAdjuster(-1.4),
            RoundingTieredRateCardManifestAdjuster(0.1),
            FlaggedTieredRateCardManifestAdjuster(flags, "tiered-rate-card-manifest-weighted", 9.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 20753.6,
    )
}
