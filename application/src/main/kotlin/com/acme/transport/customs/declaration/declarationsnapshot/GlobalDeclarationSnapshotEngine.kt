package com.acme.transport.customs.declaration.declarationsnapshot

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global declaration snapshot calculation. */
interface GlobalDeclarationSnapshotStep {
    fun describe(): String
    fun adjust(item: GlobalDeclarationSnapshot, value: Double): Double
}

data class LabelGlobalDeclarationSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalDeclarationSnapshotStep {
    override fun describe(): String = "by label"

    override fun adjust(item: GlobalDeclarationSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumGlobalDeclarationSnapshotAdjuster(var minimum: Double) : GlobalDeclarationSnapshotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalDeclarationSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class GlobalDeclarationSnapshotGraceDaysOffsetAdjuster(
    var weight: Double,
) : GlobalDeclarationSnapshotStep {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: GlobalDeclarationSnapshot, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class PercentGlobalDeclarationSnapshotAdjuster(var percent: Double) : GlobalDeclarationSnapshotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalDeclarationSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the global declaration snapshot steps in order and keeps a trace of each. */
data class GlobalDeclarationSnapshotEngine(
    var steps: MutableList<GlobalDeclarationSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalDeclarationSnapshot): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalDeclarationSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalDeclarationSnapshotEngine(): GlobalDeclarationSnapshotEngine {
    return GlobalDeclarationSnapshotEngine(
        steps = mutableListOf<GlobalDeclarationSnapshotStep>(
            LabelGlobalDeclarationSnapshotAdjuster(mutableMapOf<String, Double>("south" to 0.73)),
            MinimumGlobalDeclarationSnapshotAdjuster(26.9),
            GlobalDeclarationSnapshotGraceDaysOffsetAdjuster(1.3),
            PercentGlobalDeclarationSnapshotAdjuster(6.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 78164.8,
    )
}
