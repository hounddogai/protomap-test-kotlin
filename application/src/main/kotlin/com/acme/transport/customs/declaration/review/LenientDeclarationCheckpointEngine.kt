package com.acme.transport.customs.declaration.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient declaration checkpoint calculation. */
interface LenientDeclarationCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: LenientDeclarationCheckpoint, value: Double): Double
}

data class LabelLenientDeclarationCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientDeclarationCheckpointAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: LenientDeclarationCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLenientDeclarationCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientDeclarationCheckpointAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientDeclarationCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLenientDeclarationCheckpointAdjuster(var cap: Double) : LenientDeclarationCheckpointAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientDeclarationCheckpoint, value: Double): Double = minOf(value, cap)
}

data class LenientDeclarationCheckpointUpdatedAtOffsetAdjuster(
    var weight: Double,
) : LenientDeclarationCheckpointAdjuster {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: LenientDeclarationCheckpoint, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class TieredLenientDeclarationCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientDeclarationCheckpointAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientDeclarationCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lenient declaration checkpoint steps in order and keeps a trace of each. */
data class LenientDeclarationCheckpointEngine(
    var steps: MutableList<LenientDeclarationCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientDeclarationCheckpoint): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientDeclarationCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientDeclarationCheckpointEngine(flags: Registry): LenientDeclarationCheckpointEngine {
    return LenientDeclarationCheckpointEngine(
        steps = mutableListOf<LenientDeclarationCheckpointAdjuster>(
            LabelLenientDeclarationCheckpointAdjuster(mutableMapOf<String, Double>("south" to 0.95)),
            FlaggedLenientDeclarationCheckpointAdjuster(flags, "lenient-declaration-checkpoint-global", 4.2),
            CapLenientDeclarationCheckpointAdjuster(17910.0),
            LenientDeclarationCheckpointUpdatedAtOffsetAdjuster(1.27),
            TieredLenientDeclarationCheckpointAdjuster(
                mutableListOf<Double>(6745.0, 7327.0, 8349.0),
                mutableListOf<Double>(0.93, 0.92, 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 45694.7,
    )
}
