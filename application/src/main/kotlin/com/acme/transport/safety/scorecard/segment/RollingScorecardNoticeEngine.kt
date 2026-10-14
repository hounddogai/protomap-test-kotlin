package com.acme.transport.safety.scorecard.segment

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling scorecard notice calculation. */
interface RollingScorecardNoticeModifier {
    fun describe(): String
    fun adjust(item: RollingScorecardNotice, value: Double): Double
}

data class PercentRollingScorecardNoticeAdjuster(var percent: Double) : RollingScorecardNoticeModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingScorecardNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRollingScorecardNoticeAdjuster(var cap: Double) : RollingScorecardNoticeModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingScorecardNotice, value: Double): Double = minOf(value, cap)
}

data class RollingScorecardNoticeAmountOffsetAdjuster(var weight: Double) : RollingScorecardNoticeModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: RollingScorecardNotice, value: Double): Double = value + item.amount * weight
}

/** Runs the rolling scorecard notice steps in order and keeps a trace of each. */
data class RollingScorecardNoticeEngine(
    var steps: MutableList<RollingScorecardNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingScorecardNotice): Double {
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

    fun evaluateAll(items: MutableList<RollingScorecardNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingScorecardNoticeEngine(): RollingScorecardNoticeEngine {
    return RollingScorecardNoticeEngine(
        steps = mutableListOf<RollingScorecardNoticeModifier>(
            PercentRollingScorecardNoticeAdjuster(16.1),
            CapRollingScorecardNoticeAdjuster(48526.0),
            RollingScorecardNoticeAmountOffsetAdjuster(0.53),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 78044.9,
    )
}
