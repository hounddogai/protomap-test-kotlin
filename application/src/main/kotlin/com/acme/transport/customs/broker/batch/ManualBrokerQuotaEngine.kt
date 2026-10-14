package com.acme.transport.customs.broker.batch

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual broker quota calculation. */
interface ManualBrokerQuotaAdjuster {
    fun describe(): String
    fun adjust(item: ManualBrokerQuota, value: Double): Double
}

data class CapManualBrokerQuotaAdjuster(var cap: Double) : ManualBrokerQuotaAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualBrokerQuota, value: Double): Double = minOf(value, cap)
}

data class PercentManualBrokerQuotaAdjuster(var percent: Double) : ManualBrokerQuotaAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualBrokerQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingManualBrokerQuotaAdjuster(var step: Double) : ManualBrokerQuotaAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualBrokerQuota, value: Double): Double = roundTo(value, step)
}

data class MinimumManualBrokerQuotaAdjuster(var minimum: Double) : ManualBrokerQuotaAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualBrokerQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameManualBrokerQuotaAdjuster(var factors: MutableMap<String, Double>) : ManualBrokerQuotaAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: ManualBrokerQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual broker quota steps in order and keeps a trace of each. */
data class ManualBrokerQuotaEngine(
    var steps: MutableList<ManualBrokerQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualBrokerQuota): Double {
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

    fun evaluateAll(items: MutableList<ManualBrokerQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualBrokerQuotaEngine(): ManualBrokerQuotaEngine {
    return ManualBrokerQuotaEngine(
        steps = mutableListOf<ManualBrokerQuotaAdjuster>(
            CapManualBrokerQuotaAdjuster(25308.0),
            PercentManualBrokerQuotaAdjuster(24.9),
            RoundingManualBrokerQuotaAdjuster(0.25),
            MinimumManualBrokerQuotaAdjuster(27.7),
            NameManualBrokerQuotaAdjuster(
                mutableMapOf<String, Double>("west" to 1.32, "east" to 0.95, "central" to 0.95),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 11066.7,
    )
}
