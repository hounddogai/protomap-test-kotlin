package com.acme.transport.freightpay.ratecard.cardgroup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy rate card group calculation. */
interface LegacyRateCardGroupAdjuster {
    fun describe(): String
    fun adjust(item: LegacyRateCardGroup, value: Double): Double
}

data class LegacyRateCardGroupEtaMinutesOffsetAdjuster(var weight: Double) : LegacyRateCardGroupAdjuster {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: LegacyRateCardGroup, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class RoundingLegacyRateCardGroupAdjuster(var step: Double) : LegacyRateCardGroupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyRateCardGroup, value: Double): Double = roundTo(value, step)
}

data class FlaggedLegacyRateCardGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyRateCardGroupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyRateCardGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the legacy rate card group steps in order and keeps a trace of each. */
data class LegacyRateCardGroupEngine(
    var steps: MutableList<LegacyRateCardGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyRateCardGroup): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyRateCardGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyRateCardGroupEngine(flags: Registry): LegacyRateCardGroupEngine {
    return LegacyRateCardGroupEngine(
        steps = mutableListOf<LegacyRateCardGroupAdjuster>(
            LegacyRateCardGroupEtaMinutesOffsetAdjuster(0.91),
            RoundingLegacyRateCardGroupAdjuster(1.0),
            FlaggedLegacyRateCardGroupAdjuster(flags, "legacy-rate-card-group-tiered", -5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 96505.7,
    )
}
