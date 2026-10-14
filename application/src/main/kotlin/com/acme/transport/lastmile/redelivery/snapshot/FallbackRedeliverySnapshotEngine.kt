package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback redelivery snapshot calculation. */
interface FallbackRedeliverySnapshotModifier {
    fun describe(): String
    fun adjust(item: FallbackRedeliverySnapshot, value: Double): Double
}

data class RoundingFallbackRedeliverySnapshotAdjuster(var step: Double) : FallbackRedeliverySnapshotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackRedeliverySnapshot, value: Double): Double = roundTo(value, step)
}

data class DepotFallbackRedeliverySnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackRedeliverySnapshotModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: FallbackRedeliverySnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFallbackRedeliverySnapshotAdjuster(var minimum: Double) : FallbackRedeliverySnapshotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackRedeliverySnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FallbackRedeliverySnapshotQuantityOffsetAdjuster(
    var weight: Double,
) : FallbackRedeliverySnapshotModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: FallbackRedeliverySnapshot, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

/** Runs the fallback redelivery snapshot steps in order and keeps a trace of each. */
data class FallbackRedeliverySnapshotEngine(
    var steps: MutableList<FallbackRedeliverySnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackRedeliverySnapshot): Double {
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

    fun evaluateAll(items: MutableList<FallbackRedeliverySnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackRedeliverySnapshotEngine(): FallbackRedeliverySnapshotEngine {
    return FallbackRedeliverySnapshotEngine(
        steps = mutableListOf<FallbackRedeliverySnapshotModifier>(
            RoundingFallbackRedeliverySnapshotAdjuster(0.5),
            DepotFallbackRedeliverySnapshotAdjuster(
                mutableMapOf<String, Double>("west" to 0.87, "south" to 1.3),
            ),
            MinimumFallbackRedeliverySnapshotAdjuster(32.8),
            FallbackRedeliverySnapshotQuantityOffsetAdjuster(0.86),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 29677.4,
    )
}
