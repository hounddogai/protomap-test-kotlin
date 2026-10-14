package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped tender window calculation. */
interface CappedTenderWindowModifier {
    fun describe(): String
    fun adjust(item: CappedTenderWindow, value: Double): Double
}

data class PercentCappedTenderWindowAdjuster(var percent: Double) : CappedTenderWindowModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedTenderWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCappedTenderWindowAdjuster(var cap: Double) : CappedTenderWindowModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedTenderWindow, value: Double): Double = minOf(value, cap)
}

data class CappedTenderWindowQuantityOffsetAdjuster(var weight: Double) : CappedTenderWindowModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: CappedTenderWindow, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class RoundingCappedTenderWindowAdjuster(var step: Double) : CappedTenderWindowModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedTenderWindow, value: Double): Double = roundTo(value, step)
}

data class NameCappedTenderWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedTenderWindowModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: CappedTenderWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped tender window steps in order and keeps a trace of each. */
data class CappedTenderWindowEngine(
    var steps: MutableList<CappedTenderWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedTenderWindow): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedTenderWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedTenderWindowEngine(): CappedTenderWindowEngine {
    return CappedTenderWindowEngine(
        steps = mutableListOf<CappedTenderWindowModifier>(
            PercentCappedTenderWindowAdjuster(-1.3),
            CapCappedTenderWindowAdjuster(36357.0),
            CappedTenderWindowQuantityOffsetAdjuster(1.74),
            RoundingCappedTenderWindowAdjuster(0.05),
            NameCappedTenderWindowAdjuster(mutableMapOf<String, Double>("east" to 0.82, "south" to 1.0)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 99437.4,
    )
}
