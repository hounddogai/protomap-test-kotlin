package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the cutoff agreement calculation. */
interface CutoffAgreementAdjuster {
    fun describe(): String
    fun adjust(item: CutoffAgreement, value: Double): Double
}

data class MinimumCutoffAgreementAdjuster(var minimum: Double) : CutoffAgreementAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CutoffAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCutoffAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CutoffAgreementAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CutoffAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionCutoffAgreementAdjuster(var factors: MutableMap<String, Double>) : CutoffAgreementAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: CutoffAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCutoffAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CutoffAgreementAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CutoffAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCutoffAgreementAdjuster(var cap: Double) : CutoffAgreementAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CutoffAgreement, value: Double): Double = minOf(value, cap)
}

/** Runs the cutoff agreement steps in order and keeps a trace of each. */
data class CutoffAgreementEngine(
    var steps: MutableList<CutoffAgreementAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CutoffAgreement): Double {
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

    fun evaluateAll(items: MutableList<CutoffAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCutoffAgreementEngine(flags: Registry): CutoffAgreementEngine {
    return CutoffAgreementEngine(
        steps = mutableListOf<CutoffAgreementAdjuster>(
            MinimumCutoffAgreementAdjuster(2.3),
            FlaggedCutoffAgreementAdjuster(flags, "cutoff-agreement-fallback", 4.8),
            RegionCutoffAgreementAdjuster(mutableMapOf<String, Double>("retail" to 1.13, "west" to 1.06)),
            TieredCutoffAgreementAdjuster(
                mutableListOf<Double>(3336.0, 6660.0, 6704.0),
                mutableListOf<Double>(1.19, 1.18, 0.86),
            ),
            CapCutoffAgreementAdjuster(3922.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 21963.0,
    )
}
