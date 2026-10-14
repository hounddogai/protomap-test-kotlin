package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cutoff profile calculation. */
interface CutoffProfileAdjuster {
    fun describe(): String
    fun adjust(item: CutoffProfile, value: Double): Double
}

data class RoundingCutoffProfileAdjuster(var step: Double) : CutoffProfileAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CutoffProfile, value: Double): Double = roundTo(value, step)
}

data class FlaggedCutoffProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CutoffProfileAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CutoffProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCutoffProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CutoffProfileAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CutoffProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCutoffProfileAdjuster(var cap: Double) : CutoffProfileAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CutoffProfile, value: Double): Double = minOf(value, cap)
}

data class SourceCutoffProfileAdjuster(var factors: MutableMap<String, Double>) : CutoffProfileAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: CutoffProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the cutoff profile steps in order and keeps a trace of each. */
data class CutoffProfileEngine(
    var steps: MutableList<CutoffProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CutoffProfile): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CutoffProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCutoffProfileEngine(flags: Registry): CutoffProfileEngine {
    return CutoffProfileEngine(
        steps = mutableListOf<CutoffProfileAdjuster>(
            RoundingCutoffProfileAdjuster(0.05),
            FlaggedCutoffProfileAdjuster(flags, "cutoff-profile-adaptive", -6.2),
            TieredCutoffProfileAdjuster(
                mutableListOf<Double>(3747.0, 8079.0, 8575.0),
                mutableListOf<Double>(1.13, 0.97, 0.84),
            ),
            CapCutoffProfileAdjuster(22654.0),
            SourceCutoffProfileAdjuster(mutableMapOf<String, Double>("south" to 1.27)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 52509.4,
    )
}
