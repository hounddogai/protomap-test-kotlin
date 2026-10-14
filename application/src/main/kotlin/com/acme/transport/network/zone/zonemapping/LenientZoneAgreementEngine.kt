package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient zone agreement calculation. */
interface LenientZoneAgreementFactor {
    fun describe(): String
    fun adjust(item: LenientZoneAgreement, value: Double): Double
}

data class PercentLenientZoneAgreementAdjuster(var percent: Double) : LenientZoneAgreementFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientZoneAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LenientZoneAgreementStopCountOffsetAdjuster(var weight: Double) : LenientZoneAgreementFactor {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: LenientZoneAgreement, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class FlaggedLenientZoneAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientZoneAgreementFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientZoneAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLenientZoneAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientZoneAgreementFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientZoneAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SourceLenientZoneAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientZoneAgreementFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: LenientZoneAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLenientZoneAgreementAdjuster(var cap: Double) : LenientZoneAgreementFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientZoneAgreement, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient zone agreement steps in order and keeps a trace of each. */
data class LenientZoneAgreementEngine(
    var steps: MutableList<LenientZoneAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientZoneAgreement): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientZoneAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientZoneAgreementEngine(flags: Registry): LenientZoneAgreementEngine {
    return LenientZoneAgreementEngine(
        steps = mutableListOf<LenientZoneAgreementFactor>(
            PercentLenientZoneAgreementAdjuster(-9.7),
            LenientZoneAgreementStopCountOffsetAdjuster(1.59),
            FlaggedLenientZoneAgreementAdjuster(flags, "lenient-zone-agreement-seasonal", 0.8),
            TieredLenientZoneAgreementAdjuster(
                mutableListOf<Double>(1251.0, 3648.0, 5100.0, 8139.0),
                mutableListOf<Double>(1.16, 1.09, 0.86, 1.17),
            ),
            SourceLenientZoneAgreementAdjuster(mutableMapOf<String, Double>("retail" to 0.84)),
            CapLenientZoneAgreementAdjuster(2306.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 96417.8,
    )
}
