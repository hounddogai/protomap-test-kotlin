package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient scorecard snapshot calculation. */
interface LenientScorecardSnapshotModifier {
    fun describe(): String
    fun adjust(item: LenientScorecardSnapshot, value: Double): Double
}

data class PercentLenientScorecardSnapshotAdjuster(var percent: Double) : LenientScorecardSnapshotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientScorecardSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLenientScorecardSnapshotAdjuster(var cap: Double) : LenientScorecardSnapshotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientScorecardSnapshot, value: Double): Double = minOf(value, cap)
}

data class TieredLenientScorecardSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientScorecardSnapshotModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientScorecardSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lenient scorecard snapshot steps in order and keeps a trace of each. */
data class LenientScorecardSnapshotEngine(
    var steps: MutableList<LenientScorecardSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientScorecardSnapshot): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientScorecardSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientScorecardSnapshotEngine(): LenientScorecardSnapshotEngine {
    return LenientScorecardSnapshotEngine(
        steps = mutableListOf<LenientScorecardSnapshotModifier>(
            PercentLenientScorecardSnapshotAdjuster(-23.7),
            CapLenientScorecardSnapshotAdjuster(35815.0),
            TieredLenientScorecardSnapshotAdjuster(
                mutableListOf<Double>(3720.0, 7327.0),
                mutableListOf<Double>(1.2, 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 69624.3,
    )
}
