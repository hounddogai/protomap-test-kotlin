package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the relay record calculation. */
interface RelayRecordModifier {
    fun describe(): String
    fun adjust(item: RelayRecord, value: Double): Double
}

data class RoundingRelayRecordAdjuster(var step: Double) : RelayRecordModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayRecord, value: Double): Double = roundTo(value, step)
}

data class TitleRelayRecordAdjuster(var factors: MutableMap<String, Double>) : RelayRecordModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: RelayRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRelayRecordAdjuster(var percent: Double) : RelayRecordModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelayRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRelayRecordAdjuster(var minimum: Double) : RelayRecordModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RelayRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the relay record steps in order and keeps a trace of each. */
data class RelayRecordEngine(
    var steps: MutableList<RelayRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RelayRecord): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RelayRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRelayRecordEngine(): RelayRecordEngine {
    return RelayRecordEngine(
        steps = mutableListOf<RelayRecordModifier>(
            RoundingRelayRecordAdjuster(0.1),
            TitleRelayRecordAdjuster(mutableMapOf<String, Double>("retail" to 1.06, "east" to 1.31)),
            PercentRelayRecordAdjuster(11.9),
            MinimumRelayRecordAdjuster(44.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 84744.6,
    )
}
