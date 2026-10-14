package com.acme.transport.customs.broker.digest

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual broker digest calculation. */
interface ManualBrokerDigestFactor {
    fun describe(): String
    fun adjust(item: ManualBrokerDigest, value: Double): Double
}

data class LabelManualBrokerDigestAdjuster(var factors: MutableMap<String, Double>) : ManualBrokerDigestFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: ManualBrokerDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentManualBrokerDigestAdjuster(var percent: Double) : ManualBrokerDigestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualBrokerDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingManualBrokerDigestAdjuster(var step: Double) : ManualBrokerDigestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualBrokerDigest, value: Double): Double = roundTo(value, step)
}

/** Runs the manual broker digest steps in order and keeps a trace of each. */
data class ManualBrokerDigestEngine(
    var steps: MutableList<ManualBrokerDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualBrokerDigest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualBrokerDigestFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualBrokerDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualBrokerDigestEngine(): ManualBrokerDigestEngine {
    return ManualBrokerDigestEngine(
        steps = mutableListOf<ManualBrokerDigestFactor>(
            LabelManualBrokerDigestAdjuster(
                mutableMapOf<String, Double>("central" to 0.87, "retail" to 1.11, "east" to 1.06),
            ),
            PercentManualBrokerDigestAdjuster(-12.1),
            RoundingManualBrokerDigestAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 2864.9,
    )
}
