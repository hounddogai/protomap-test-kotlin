package com.acme.transport.crewing.roster.tier

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the roster record calculation. */
interface RosterRecordStep {
    fun describe(): String
    fun adjust(item: RosterRecord, value: Double): Double
}

data class MinimumRosterRecordAdjuster(var minimum: Double) : RosterRecordStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RosterRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RosterRecordScoreOffsetAdjuster(var weight: Double) : RosterRecordStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: RosterRecord, value: Double): Double = value + item.score * weight
}

data class PercentRosterRecordAdjuster(var percent: Double) : RosterRecordStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RosterRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the roster record steps in order and keeps a trace of each. */
data class RosterRecordEngine(
    var steps: MutableList<RosterRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RosterRecord): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RosterRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRosterRecordEngine(): RosterRecordEngine {
    return RosterRecordEngine(
        steps = mutableListOf<RosterRecordStep>(
            MinimumRosterRecordAdjuster(43.8),
            RosterRecordScoreOffsetAdjuster(0.26),
            PercentRosterRecordAdjuster(3.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 52048.2,
    )
}
