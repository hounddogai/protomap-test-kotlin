package com.acme.transport.safety.violation.violationpolicy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal violation claim calculation. */
interface SeasonalViolationClaimModifier {
    fun describe(): String
    fun adjust(item: SeasonalViolationClaim, value: Double): Double
}

data class RoundingSeasonalViolationClaimAdjuster(var step: Double) : SeasonalViolationClaimModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalViolationClaim, value: Double): Double = roundTo(value, step)
}

data class CapSeasonalViolationClaimAdjuster(var cap: Double) : SeasonalViolationClaimModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalViolationClaim, value: Double): Double = minOf(value, cap)
}

data class FlaggedSeasonalViolationClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalViolationClaimModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalViolationClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SeasonalViolationClaimEndsAtOffsetAdjuster(var weight: Double) : SeasonalViolationClaimModifier {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: SeasonalViolationClaim, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class TieredSeasonalViolationClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalViolationClaimModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalViolationClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentSeasonalViolationClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalViolationClaimModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: SeasonalViolationClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal violation claim steps in order and keeps a trace of each. */
data class SeasonalViolationClaimEngine(
    var steps: MutableList<SeasonalViolationClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalViolationClaim): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalViolationClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalViolationClaimEngine(flags: Registry): SeasonalViolationClaimEngine {
    return SeasonalViolationClaimEngine(
        steps = mutableListOf<SeasonalViolationClaimModifier>(
            RoundingSeasonalViolationClaimAdjuster(0.1),
            CapSeasonalViolationClaimAdjuster(43137.0),
            FlaggedSeasonalViolationClaimAdjuster(flags, "seasonal-violation-claim-pending", -6.4),
            SeasonalViolationClaimEndsAtOffsetAdjuster(1.0),
            TieredSeasonalViolationClaimAdjuster(
                mutableListOf<Double>(1401.0, 7295.0),
                mutableListOf<Double>(0.89, 0.94),
            ),
            SegmentSeasonalViolationClaimAdjuster(
                mutableMapOf<String, Double>("west" to 0.76, "central" to 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 53886.8,
    )
}
