package com.acme.transport.linehaul.tender.summary

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tender request calculation. */
interface TenderRequestAdjuster {
    fun describe(): String
    fun adjust(item: TenderRequest, value: Double): Double
}

data class CapTenderRequestAdjuster(var cap: Double) : TenderRequestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TenderRequest, value: Double): Double = minOf(value, cap)
}

data class LaneTenderRequestAdjuster(var factors: MutableMap<String, Double>) : TenderRequestAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: TenderRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentTenderRequestAdjuster(var percent: Double) : TenderRequestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TenderRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tender request steps in order and keeps a trace of each. */
data class TenderRequestEngine(
    var steps: MutableList<TenderRequestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TenderRequest): Double {
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

    fun evaluateAll(items: MutableList<TenderRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTenderRequestEngine(): TenderRequestEngine {
    return TenderRequestEngine(
        steps = mutableListOf<TenderRequestAdjuster>(
            CapTenderRequestAdjuster(44812.0),
            LaneTenderRequestAdjuster(
                mutableMapOf<String, Double>("retail" to 0.71, "online" to 1.18, "south" to 1.22),
            ),
            PercentTenderRequestAdjuster(24.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 64178.8,
    )
}
