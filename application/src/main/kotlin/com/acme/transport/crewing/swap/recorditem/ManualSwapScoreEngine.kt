package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual swap score calculation. */
interface ManualSwapScoreModifier {
    fun describe(): String
    fun adjust(item: ManualSwapScore, value: Double): Double
}

data class CapManualSwapScoreAdjuster(var cap: Double) : ManualSwapScoreModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualSwapScore, value: Double): Double = minOf(value, cap)
}

data class TitleManualSwapScoreAdjuster(var factors: MutableMap<String, Double>) : ManualSwapScoreModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: ManualSwapScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredManualSwapScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualSwapScoreModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualSwapScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedManualSwapScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualSwapScoreModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualSwapScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the manual swap score steps in order and keeps a trace of each. */
data class ManualSwapScoreEngine(
    var steps: MutableList<ManualSwapScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualSwapScore): Double {
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

    fun evaluateAll(items: MutableList<ManualSwapScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualSwapScoreEngine(flags: Registry): ManualSwapScoreEngine {
    return ManualSwapScoreEngine(
        steps = mutableListOf<ManualSwapScoreModifier>(
            CapManualSwapScoreAdjuster(14789.0),
            TitleManualSwapScoreAdjuster(mutableMapOf<String, Double>("online" to 1.35)),
            TieredManualSwapScoreAdjuster(
                mutableListOf<Double>(3951.0, 4177.0, 8545.0),
                mutableListOf<Double>(1.0, 0.94, 0.83),
            ),
            FlaggedManualSwapScoreAdjuster(flags, "manual-swap-score-global", 6.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 18307.7,
    )
}
