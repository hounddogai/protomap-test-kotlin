package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy swap agreement calculation. */
interface LegacySwapAgreementStep {
    fun describe(): String
    fun adjust(item: LegacySwapAgreement, value: Double): Double
}

data class CapLegacySwapAgreementAdjuster(var cap: Double) : LegacySwapAgreementStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacySwapAgreement, value: Double): Double = minOf(value, cap)
}

data class FlaggedLegacySwapAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacySwapAgreementStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacySwapAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LegacySwapAgreementStopCountOffsetAdjuster(var weight: Double) : LegacySwapAgreementStep {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: LegacySwapAgreement, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class DepotLegacySwapAgreementAdjuster(var factors: MutableMap<String, Double>) : LegacySwapAgreementStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: LegacySwapAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the legacy swap agreement steps in order and keeps a trace of each. */
data class LegacySwapAgreementEngine(
    var steps: MutableList<LegacySwapAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacySwapAgreement): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacySwapAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacySwapAgreementEngine(flags: Registry): LegacySwapAgreementEngine {
    return LegacySwapAgreementEngine(
        steps = mutableListOf<LegacySwapAgreementStep>(
            CapLegacySwapAgreementAdjuster(27317.0),
            FlaggedLegacySwapAgreementAdjuster(flags, "legacy-swap-agreement-tiered", 0.0),
            LegacySwapAgreementStopCountOffsetAdjuster(1.02),
            DepotLegacySwapAgreementAdjuster(mutableMapOf<String, Double>("south" to 1.04, "central" to 1.06)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 37039.9,
    )
}
