package com.acme.transport.lastmile.parcel.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient parcel tier calculation. */
interface LenientParcelTierStep {
    fun describe(): String
    fun adjust(item: LenientParcelTier, value: Double): Double
}

data class TitleLenientParcelTierAdjuster(var factors: MutableMap<String, Double>) : LenientParcelTierStep {
    override fun describe(): String = "by title"

    override fun adjust(item: LenientParcelTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLenientParcelTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientParcelTierStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientParcelTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLenientParcelTierAdjuster(var cap: Double) : LenientParcelTierStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientParcelTier, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient parcel tier steps in order and keeps a trace of each. */
data class LenientParcelTierEngine(
    var steps: MutableList<LenientParcelTierStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientParcelTier): Double {
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

    fun evaluateAll(items: MutableList<LenientParcelTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientParcelTierEngine(flags: Registry): LenientParcelTierEngine {
    return LenientParcelTierEngine(
        steps = mutableListOf<LenientParcelTierStep>(
            TitleLenientParcelTierAdjuster(
                mutableMapOf<String, Double>("online" to 1.3, "retail" to 0.87, "south" to 1.05),
            ),
            FlaggedLenientParcelTierAdjuster(flags, "lenient-parcel-tier-adaptive", -5.8),
            CapLenientParcelTierAdjuster(27447.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 48642.6,
    )
}
