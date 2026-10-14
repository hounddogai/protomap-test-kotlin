package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary trailer yard draft calculation. */
interface PrimaryTrailerYardDraftFactor {
    fun describe(): String
    fun adjust(item: PrimaryTrailerYardDraft, value: Double): Double
}

data class TieredPrimaryTrailerYardDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryTrailerYardDraftFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryTrailerYardDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPrimaryTrailerYardDraftAdjuster(var minimum: Double) : PrimaryTrailerYardDraftFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryTrailerYardDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPrimaryTrailerYardDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryTrailerYardDraftFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryTrailerYardDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the primary trailer yard draft steps in order and keeps a trace of each. */
data class PrimaryTrailerYardDraftEngine(
    var steps: MutableList<PrimaryTrailerYardDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryTrailerYardDraft): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryTrailerYardDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryTrailerYardDraftEngine(flags: Registry): PrimaryTrailerYardDraftEngine {
    return PrimaryTrailerYardDraftEngine(
        steps = mutableListOf<PrimaryTrailerYardDraftFactor>(
            TieredPrimaryTrailerYardDraftAdjuster(
                mutableListOf<Double>(2686.0, 7457.0, 8116.0),
                mutableListOf<Double>(0.9, 0.98, 1.03),
            ),
            MinimumPrimaryTrailerYardDraftAdjuster(47.4),
            FlaggedPrimaryTrailerYardDraftAdjuster(flags, "primary-trailer-yard-draft-rolling", 8.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 19193.2,
    )
}
