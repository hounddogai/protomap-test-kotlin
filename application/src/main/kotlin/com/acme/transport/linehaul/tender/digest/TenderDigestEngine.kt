package com.acme.transport.linehaul.tender.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tender digest calculation. */
interface TenderDigestModifier {
    fun describe(): String
    fun adjust(item: TenderDigest, value: Double): Double
}

data class PercentTenderDigestAdjuster(var percent: Double) : TenderDigestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TenderDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedTenderDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TenderDigestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TenderDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumTenderDigestAdjuster(var minimum: Double) : TenderDigestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TenderDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TenderDigestScoreOffsetAdjuster(var weight: Double) : TenderDigestModifier {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: TenderDigest, value: Double): Double = value + item.score * weight
}

/** Runs the tender digest steps in order and keeps a trace of each. */
data class TenderDigestEngine(
    var steps: MutableList<TenderDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TenderDigest): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TenderDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTenderDigestEngine(flags: Registry): TenderDigestEngine {
    return TenderDigestEngine(
        steps = mutableListOf<TenderDigestModifier>(
            PercentTenderDigestAdjuster(20.6),
            FlaggedTenderDigestAdjuster(flags, "tender-digest-batched", -0.7),
            MinimumTenderDigestAdjuster(26.1),
            TenderDigestScoreOffsetAdjuster(0.85),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 22264.9,
    )
}
