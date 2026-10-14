package com.acme.transport.safety.scorecard.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary scorecard digest calculation. */
interface PrimaryScorecardDigestStep {
    fun describe(): String
    fun adjust(item: PrimaryScorecardDigest, value: Double): Double
}

data class FlaggedPrimaryScorecardDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryScorecardDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryScorecardDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPrimaryScorecardDigestAdjuster(var cap: Double) : PrimaryScorecardDigestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryScorecardDigest, value: Double): Double = minOf(value, cap)
}

data class TieredPrimaryScorecardDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryScorecardDigestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryScorecardDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary scorecard digest steps in order and keeps a trace of each. */
data class PrimaryScorecardDigestEngine(
    var steps: MutableList<PrimaryScorecardDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryScorecardDigest): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryScorecardDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryScorecardDigestEngine(flags: Registry): PrimaryScorecardDigestEngine {
    return PrimaryScorecardDigestEngine(
        steps = mutableListOf<PrimaryScorecardDigestStep>(
            FlaggedPrimaryScorecardDigestAdjuster(flags, "primary-scorecard-digest-primary", 0.6),
            CapPrimaryScorecardDigestAdjuster(1570.0),
            TieredPrimaryScorecardDigestAdjuster(
                mutableListOf<Double>(375.0, 3482.0, 6972.0),
                mutableListOf<Double>(0.89, 1.02, 0.86),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 76673.3,
    )
}
