package com.acme.transport.crewing.restbreak.segment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global rest break snapshot calculation. */
interface GlobalRestBreakSnapshotModifier {
    fun describe(): String
    fun adjust(item: GlobalRestBreakSnapshot, value: Double): Double
}

data class PercentGlobalRestBreakSnapshotAdjuster(var percent: Double) : GlobalRestBreakSnapshotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalRestBreakSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CategoryGlobalRestBreakSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalRestBreakSnapshotModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: GlobalRestBreakSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingGlobalRestBreakSnapshotAdjuster(var step: Double) : GlobalRestBreakSnapshotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalRestBreakSnapshot, value: Double): Double = roundTo(value, step)
}

data class GlobalRestBreakSnapshotAttemptsOffsetAdjuster(var weight: Double) : GlobalRestBreakSnapshotModifier {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: GlobalRestBreakSnapshot, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

/** Runs the global rest break snapshot steps in order and keeps a trace of each. */
data class GlobalRestBreakSnapshotEngine(
    var steps: MutableList<GlobalRestBreakSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalRestBreakSnapshot): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalRestBreakSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalRestBreakSnapshotEngine(): GlobalRestBreakSnapshotEngine {
    return GlobalRestBreakSnapshotEngine(
        steps = mutableListOf<GlobalRestBreakSnapshotModifier>(
            PercentGlobalRestBreakSnapshotAdjuster(14.2),
            CategoryGlobalRestBreakSnapshotAdjuster(
                mutableMapOf<String, Double>("south" to 1.37, "online" to 0.81),
            ),
            RoundingGlobalRestBreakSnapshotAdjuster(5.0),
            GlobalRestBreakSnapshotAttemptsOffsetAdjuster(1.61),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 29221.2,
    )
}
