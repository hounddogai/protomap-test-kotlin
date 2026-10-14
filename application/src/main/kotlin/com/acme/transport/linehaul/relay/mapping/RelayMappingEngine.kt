package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the relay mapping calculation. */
interface RelayMappingAdjuster {
    fun describe(): String
    fun adjust(item: RelayMapping, value: Double): Double
}

data class LabelRelayMappingAdjuster(var factors: MutableMap<String, Double>) : RelayMappingAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: RelayMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRelayMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayMappingAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRelayMappingAdjuster(var step: Double) : RelayMappingAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayMapping, value: Double): Double = roundTo(value, step)
}

data class PercentRelayMappingAdjuster(var percent: Double) : RelayMappingAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelayMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RelayMappingRevisionOffsetAdjuster(var weight: Double) : RelayMappingAdjuster {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: RelayMapping, value: Double): Double = value + item.revision.toDouble() * weight
}

/** Runs the relay mapping steps in order and keeps a trace of each. */
data class RelayMappingEngine(
    var steps: MutableList<RelayMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RelayMapping): Double {
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

    fun evaluateAll(items: MutableList<RelayMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRelayMappingEngine(): RelayMappingEngine {
    return RelayMappingEngine(
        steps = mutableListOf<RelayMappingAdjuster>(
            LabelRelayMappingAdjuster(
                mutableMapOf<String, Double>("north" to 0.78, "online" to 1.0, "east" to 1.03),
            ),
            TieredRelayMappingAdjuster(
                mutableListOf<Double>(165.0, 1819.0, 2699.0, 2771.0),
                mutableListOf<Double>(0.92, 1.15, 1.03, 0.95),
            ),
            RoundingRelayMappingAdjuster(1.0),
            PercentRelayMappingAdjuster(5.4),
            RelayMappingRevisionOffsetAdjuster(0.17),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 14542.6,
    )
}
