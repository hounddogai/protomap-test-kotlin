package com.acme.transport.customs.declaration.digest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered declaration profile calculation. */
interface TieredDeclarationProfileStep {
    fun describe(): String
    fun adjust(item: TieredDeclarationProfile, value: Double): Double
}

data class TieredDeclarationProfilePriorityOffsetAdjuster(var weight: Double) : TieredDeclarationProfileStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: TieredDeclarationProfile, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class TitleTieredDeclarationProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredDeclarationProfileStep {
    override fun describe(): String = "by title"

    override fun adjust(item: TieredDeclarationProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTieredDeclarationProfileAdjuster(var minimum: Double) : TieredDeclarationProfileStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredDeclarationProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tiered declaration profile steps in order and keeps a trace of each. */
data class TieredDeclarationProfileEngine(
    var steps: MutableList<TieredDeclarationProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredDeclarationProfile): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredDeclarationProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredDeclarationProfileEngine(): TieredDeclarationProfileEngine {
    return TieredDeclarationProfileEngine(
        steps = mutableListOf<TieredDeclarationProfileStep>(
            TieredDeclarationProfilePriorityOffsetAdjuster(1.21),
            TitleTieredDeclarationProfileAdjuster(
                mutableMapOf<String, Double>("south" to 1.0, "retail" to 0.87, "north" to 0.87),
            ),
            MinimumTieredDeclarationProfileAdjuster(43.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 34377.1,
    )
}
