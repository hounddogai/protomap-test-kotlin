package com.acme.transport.safety.nearmiss.proposal

import com.acme.transport.kit.flags.Registry

/** One step of the lenient near miss claim calculation. */
interface LenientNearMissClaimFactor {
    fun describe(): String
    fun adjust(item: LenientNearMissClaim, value: Double): Double
}

data class TitleLenientNearMissClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientNearMissClaimFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: LenientNearMissClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLenientNearMissClaimAdjuster(var cap: Double) : LenientNearMissClaimFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientNearMissClaim, value: Double): Double = minOf(value, cap)
}

data class MinimumLenientNearMissClaimAdjuster(var minimum: Double) : LenientNearMissClaimFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientNearMissClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLenientNearMissClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientNearMissClaimFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientNearMissClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient near miss claim steps in order and keeps a trace of each. */
data class LenientNearMissClaimEngine(
    var steps: MutableList<LenientNearMissClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientNearMissClaim): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientNearMissClaimFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientNearMissClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientNearMissClaimEngine(flags: Registry): LenientNearMissClaimEngine {
    return LenientNearMissClaimEngine(
        steps = mutableListOf<LenientNearMissClaimFactor>(
            TitleLenientNearMissClaimAdjuster(mutableMapOf<String, Double>("online" to 1.34, "west" to 1.01)),
            CapLenientNearMissClaimAdjuster(32874.0),
            MinimumLenientNearMissClaimAdjuster(18.4),
            FlaggedLenientNearMissClaimAdjuster(flags, "lenient-near-miss-claim-pending", 7.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 26856.6,
    )
}
