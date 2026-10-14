package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional relay notice calculation. */
interface RegionalRelayNoticeStep {
    fun describe(): String
    fun adjust(item: RegionalRelayNotice, value: Double): Double
}

data class CapRegionalRelayNoticeAdjuster(var cap: Double) : RegionalRelayNoticeStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalRelayNotice, value: Double): Double = minOf(value, cap)
}

data class NameRegionalRelayNoticeAdjuster(var factors: MutableMap<String, Double>) : RegionalRelayNoticeStep {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalRelayNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRegionalRelayNoticeAdjuster(var step: Double) : RegionalRelayNoticeStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalRelayNotice, value: Double): Double = roundTo(value, step)
}

/** Runs the regional relay notice steps in order and keeps a trace of each. */
data class RegionalRelayNoticeEngine(
    var steps: MutableList<RegionalRelayNoticeStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalRelayNotice): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalRelayNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalRelayNoticeEngine(): RegionalRelayNoticeEngine {
    return RegionalRelayNoticeEngine(
        steps = mutableListOf<RegionalRelayNoticeStep>(
            CapRegionalRelayNoticeAdjuster(17982.0),
            NameRegionalRelayNoticeAdjuster(
                mutableMapOf<String, Double>("west" to 1.0, "central" to 1.19, "south" to 1.35),
            ),
            RoundingRegionalRelayNoticeAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 29491.5,
    )
}
