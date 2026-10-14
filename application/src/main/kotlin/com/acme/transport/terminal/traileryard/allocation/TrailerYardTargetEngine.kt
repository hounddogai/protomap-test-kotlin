package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the trailer yard target calculation. */
interface TrailerYardTargetStep {
    fun describe(): String
    fun adjust(item: TrailerYardTarget, value: Double): Double
}

data class RoundingTrailerYardTargetAdjuster(var step: Double) : TrailerYardTargetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerYardTarget, value: Double): Double = roundTo(value, step)
}

data class TrailerYardTargetLimitOffsetAdjuster(var weight: Double) : TrailerYardTargetStep {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: TrailerYardTarget, value: Double): Double = value + item.limit.toDouble() * weight
}

data class PercentTrailerYardTargetAdjuster(var percent: Double) : TrailerYardTargetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerYardTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the trailer yard target steps in order and keeps a trace of each. */
data class TrailerYardTargetEngine(
    var steps: MutableList<TrailerYardTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TrailerYardTarget): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TrailerYardTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTrailerYardTargetEngine(): TrailerYardTargetEngine {
    return TrailerYardTargetEngine(
        steps = mutableListOf<TrailerYardTargetStep>(
            RoundingTrailerYardTargetAdjuster(0.5),
            TrailerYardTargetLimitOffsetAdjuster(1.98),
            PercentTrailerYardTargetAdjuster(-7.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 86208.7,
    )
}
