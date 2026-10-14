package com.acme.transport.customs.declaration.profile

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending declaration baseline calculation. */
interface PendingDeclarationBaselineFactor {
    fun describe(): String
    fun adjust(item: PendingDeclarationBaseline, value: Double): Double
}

data class UnitPendingDeclarationBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingDeclarationBaselineFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: PendingDeclarationBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPendingDeclarationBaselineAdjuster(var percent: Double) : PendingDeclarationBaselineFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingDeclarationBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPendingDeclarationBaselineAdjuster(var minimum: Double) : PendingDeclarationBaselineFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingDeclarationBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending declaration baseline steps in order and keeps a trace of each. */
data class PendingDeclarationBaselineEngine(
    var steps: MutableList<PendingDeclarationBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingDeclarationBaseline): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingDeclarationBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingDeclarationBaselineEngine(): PendingDeclarationBaselineEngine {
    return PendingDeclarationBaselineEngine(
        steps = mutableListOf<PendingDeclarationBaselineFactor>(
            UnitPendingDeclarationBaselineAdjuster(mutableMapOf<String, Double>("retail" to 1.06)),
            PercentPendingDeclarationBaselineAdjuster(20.2),
            MinimumPendingDeclarationBaselineAdjuster(28.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 33588.8,
    )
}
