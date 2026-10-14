package com.acme.transport.emissions.emissionfactor.factormilestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped emission factor draft calculation. */
interface CappedEmissionFactorDraftModifier {
    fun describe(): String
    fun adjust(item: CappedEmissionFactorDraft, value: Double): Double
}

data class TieredCappedEmissionFactorDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedEmissionFactorDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedEmissionFactorDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedCappedEmissionFactorDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedEmissionFactorDraftModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedEmissionFactorDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCappedEmissionFactorDraftAdjuster(var step: Double) : CappedEmissionFactorDraftModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedEmissionFactorDraft, value: Double): Double = roundTo(value, step)
}

/** Runs the capped emission factor draft steps in order and keeps a trace of each. */
data class CappedEmissionFactorDraftEngine(
    var steps: MutableList<CappedEmissionFactorDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedEmissionFactorDraft): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedEmissionFactorDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedEmissionFactorDraftEngine(flags: Registry): CappedEmissionFactorDraftEngine {
    return CappedEmissionFactorDraftEngine(
        steps = mutableListOf<CappedEmissionFactorDraftModifier>(
            TieredCappedEmissionFactorDraftAdjuster(
                mutableListOf<Double>(3738.0, 7637.0),
                mutableListOf<Double>(1.17, 1.2),
            ),
            FlaggedCappedEmissionFactorDraftAdjuster(flags, "capped-emission-factor-draft-rolling", -1.8),
            RoundingCappedEmissionFactorDraftAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 2748.2,
    )
}
