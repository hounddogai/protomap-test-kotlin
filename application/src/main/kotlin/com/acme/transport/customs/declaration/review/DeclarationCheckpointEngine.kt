package com.acme.transport.customs.declaration.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the declaration checkpoint calculation. */
interface DeclarationCheckpointStep {
    fun describe(): String
    fun adjust(item: DeclarationCheckpoint, value: Double): Double
}

data class CapDeclarationCheckpointAdjuster(var cap: Double) : DeclarationCheckpointStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DeclarationCheckpoint, value: Double): Double = minOf(value, cap)
}

data class FlaggedDeclarationCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DeclarationCheckpointStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DeclarationCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDeclarationCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationCheckpointStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SourceDeclarationCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : DeclarationCheckpointStep {
    override fun describe(): String = "by source"

    override fun adjust(item: DeclarationCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class DeclarationCheckpointScoreOffsetAdjuster(var weight: Double) : DeclarationCheckpointStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: DeclarationCheckpoint, value: Double): Double = value + item.score * weight
}

/** Runs the declaration checkpoint steps in order and keeps a trace of each. */
data class DeclarationCheckpointEngine(
    var steps: MutableList<DeclarationCheckpointStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DeclarationCheckpoint): Double {
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

    fun evaluateAll(items: MutableList<DeclarationCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDeclarationCheckpointEngine(flags: Registry): DeclarationCheckpointEngine {
    return DeclarationCheckpointEngine(
        steps = mutableListOf<DeclarationCheckpointStep>(
            CapDeclarationCheckpointAdjuster(38219.0),
            FlaggedDeclarationCheckpointAdjuster(flags, "declaration-checkpoint-global", -8.5),
            TieredDeclarationCheckpointAdjuster(
                mutableListOf<Double>(3565.0, 4953.0),
                mutableListOf<Double>(1.04, 1.04),
            ),
            SourceDeclarationCheckpointAdjuster(
                mutableMapOf<String, Double>("east" to 0.86, "south" to 1.17, "retail" to 1.2),
            ),
            DeclarationCheckpointScoreOffsetAdjuster(1.68),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 78543.9,
    )
}
