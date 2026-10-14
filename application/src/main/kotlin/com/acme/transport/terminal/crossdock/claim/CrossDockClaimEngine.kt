package com.acme.transport.terminal.crossdock.claim

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cross dock claim calculation. */
interface CrossDockClaimStep {
    fun describe(): String
    fun adjust(item: CrossDockClaim, value: Double): Double
}

data class CrossDockClaimExpiresAtOffsetAdjuster(var weight: Double) : CrossDockClaimStep {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: CrossDockClaim, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class RoundingCrossDockClaimAdjuster(var step: Double) : CrossDockClaimStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockClaim, value: Double): Double = roundTo(value, step)
}

data class PercentCrossDockClaimAdjuster(var percent: Double) : CrossDockClaimStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CrossDockClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleCrossDockClaimAdjuster(var factors: MutableMap<String, Double>) : CrossDockClaimStep {
    override fun describe(): String = "by title"

    override fun adjust(item: CrossDockClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the cross dock claim steps in order and keeps a trace of each. */
data class CrossDockClaimEngine(
    var steps: MutableList<CrossDockClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CrossDockClaim): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CrossDockClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCrossDockClaimEngine(): CrossDockClaimEngine {
    return CrossDockClaimEngine(
        steps = mutableListOf<CrossDockClaimStep>(
            CrossDockClaimExpiresAtOffsetAdjuster(0.96),
            RoundingCrossDockClaimAdjuster(0.25),
            PercentCrossDockClaimAdjuster(-23.6),
            TitleCrossDockClaimAdjuster(mutableMapOf<String, Double>("east" to 1.27, "retail" to 0.76)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 26186.8,
    )
}
