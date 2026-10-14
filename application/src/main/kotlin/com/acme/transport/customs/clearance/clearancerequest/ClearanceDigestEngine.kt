package com.acme.transport.customs.clearance.clearancerequest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the clearance digest calculation. */
interface ClearanceDigestAdjuster {
    fun describe(): String
    fun adjust(item: ClearanceDigest, value: Double): Double
}

data class ClearanceDigestStartsAtOffsetAdjuster(var weight: Double) : ClearanceDigestAdjuster {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: ClearanceDigest, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class RoundingClearanceDigestAdjuster(var step: Double) : ClearanceDigestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ClearanceDigest, value: Double): Double = roundTo(value, step)
}

data class FlaggedClearanceDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ClearanceDigestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ClearanceDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the clearance digest steps in order and keeps a trace of each. */
data class ClearanceDigestEngine(
    var steps: MutableList<ClearanceDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ClearanceDigest): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ClearanceDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultClearanceDigestEngine(flags: Registry): ClearanceDigestEngine {
    return ClearanceDigestEngine(
        steps = mutableListOf<ClearanceDigestAdjuster>(
            ClearanceDigestStartsAtOffsetAdjuster(0.96),
            RoundingClearanceDigestAdjuster(0.25),
            FlaggedClearanceDigestAdjuster(flags, "clearance-digest-regional", -5.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 64421.1,
    )
}
