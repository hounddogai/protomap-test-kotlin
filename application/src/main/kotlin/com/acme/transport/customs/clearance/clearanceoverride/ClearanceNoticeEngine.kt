package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the clearance notice calculation. */
interface ClearanceNoticeModifier {
    fun describe(): String
    fun adjust(item: ClearanceNotice, value: Double): Double
}

data class CapClearanceNoticeAdjuster(var cap: Double) : ClearanceNoticeModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ClearanceNotice, value: Double): Double = minOf(value, cap)
}

data class RoundingClearanceNoticeAdjuster(var step: Double) : ClearanceNoticeModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ClearanceNotice, value: Double): Double = roundTo(value, step)
}

data class PercentClearanceNoticeAdjuster(var percent: Double) : ClearanceNoticeModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ClearanceNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the clearance notice steps in order and keeps a trace of each. */
data class ClearanceNoticeEngine(
    var steps: MutableList<ClearanceNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ClearanceNotice): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ClearanceNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultClearanceNoticeEngine(): ClearanceNoticeEngine {
    return ClearanceNoticeEngine(
        steps = mutableListOf<ClearanceNoticeModifier>(
            CapClearanceNoticeAdjuster(37806.0),
            RoundingClearanceNoticeAdjuster(0.25),
            PercentClearanceNoticeAdjuster(21.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 25274.0,
    )
}
