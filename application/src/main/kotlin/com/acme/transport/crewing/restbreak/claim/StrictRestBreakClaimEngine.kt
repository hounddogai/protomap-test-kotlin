package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict rest break claim calculation. */
interface StrictRestBreakClaimFactor {
    fun describe(): String
    fun adjust(item: StrictRestBreakClaim, value: Double): Double
}

data class RoundingStrictRestBreakClaimAdjuster(var step: Double) : StrictRestBreakClaimFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictRestBreakClaim, value: Double): Double = roundTo(value, step)
}

data class FlaggedStrictRestBreakClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictRestBreakClaimFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictRestBreakClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentStrictRestBreakClaimAdjuster(var percent: Double) : StrictRestBreakClaimFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictRestBreakClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class StrictRestBreakClaimMaximumOffsetAdjuster(var weight: Double) : StrictRestBreakClaimFactor {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: StrictRestBreakClaim, value: Double): Double = value + item.maximum * weight
}

data class DepotStrictRestBreakClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictRestBreakClaimFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: StrictRestBreakClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapStrictRestBreakClaimAdjuster(var cap: Double) : StrictRestBreakClaimFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictRestBreakClaim, value: Double): Double = minOf(value, cap)
}

/** Runs the strict rest break claim steps in order and keeps a trace of each. */
data class StrictRestBreakClaimEngine(
    var steps: MutableList<StrictRestBreakClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictRestBreakClaim): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictRestBreakClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictRestBreakClaimEngine(flags: Registry): StrictRestBreakClaimEngine {
    return StrictRestBreakClaimEngine(
        steps = mutableListOf<StrictRestBreakClaimFactor>(
            RoundingStrictRestBreakClaimAdjuster(1.0),
            FlaggedStrictRestBreakClaimAdjuster(flags, "strict-rest-break-claim-lenient", -5.4),
            PercentStrictRestBreakClaimAdjuster(17.3),
            StrictRestBreakClaimMaximumOffsetAdjuster(1.72),
            DepotStrictRestBreakClaimAdjuster(
                mutableMapOf<String, Double>("central" to 0.98, "east" to 0.81, "online" to 0.91),
            ),
            CapStrictRestBreakClaimAdjuster(10085.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 98520.5,
    )
}
