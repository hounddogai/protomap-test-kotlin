package com.acme.transport.customs.declaration.declarationbatch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal declaration batch calculation. */
interface SeasonalDeclarationBatchModifier {
    fun describe(): String
    fun adjust(item: SeasonalDeclarationBatch, value: Double): Double
}

data class SeasonalDeclarationBatchMaximumOffsetAdjuster(
    var weight: Double,
) : SeasonalDeclarationBatchModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: SeasonalDeclarationBatch, value: Double): Double = value + item.maximum * weight
}

data class FlaggedSeasonalDeclarationBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalDeclarationBatchModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalDeclarationBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapSeasonalDeclarationBatchAdjuster(var cap: Double) : SeasonalDeclarationBatchModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalDeclarationBatch, value: Double): Double = minOf(value, cap)
}

data class RoundingSeasonalDeclarationBatchAdjuster(var step: Double) : SeasonalDeclarationBatchModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalDeclarationBatch, value: Double): Double = roundTo(value, step)
}

data class PercentSeasonalDeclarationBatchAdjuster(var percent: Double) : SeasonalDeclarationBatchModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalDeclarationBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal declaration batch steps in order and keeps a trace of each. */
data class SeasonalDeclarationBatchEngine(
    var steps: MutableList<SeasonalDeclarationBatchModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalDeclarationBatch): Double {
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

    fun evaluateAll(items: MutableList<SeasonalDeclarationBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalDeclarationBatchEngine(flags: Registry): SeasonalDeclarationBatchEngine {
    return SeasonalDeclarationBatchEngine(
        steps = mutableListOf<SeasonalDeclarationBatchModifier>(
            SeasonalDeclarationBatchMaximumOffsetAdjuster(0.43),
            FlaggedSeasonalDeclarationBatchAdjuster(flags, "seasonal-declaration-batch-default", -6.2),
            CapSeasonalDeclarationBatchAdjuster(10187.0),
            RoundingSeasonalDeclarationBatchAdjuster(0.5),
            PercentSeasonalDeclarationBatchAdjuster(13.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 4526.1,
    )
}
