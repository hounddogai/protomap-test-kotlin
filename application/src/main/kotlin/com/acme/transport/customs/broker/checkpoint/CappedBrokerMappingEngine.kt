package com.acme.transport.customs.broker.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped broker mapping calculation. */
interface CappedBrokerMappingStep {
    fun describe(): String
    fun adjust(item: CappedBrokerMapping, value: Double): Double
}

data class FlaggedCappedBrokerMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedBrokerMappingStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedBrokerMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelCappedBrokerMappingAdjuster(var factors: MutableMap<String, Double>) : CappedBrokerMappingStep {
    override fun describe(): String = "by label"

    override fun adjust(item: CappedBrokerMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCappedBrokerMappingAdjuster(var minimum: Double) : CappedBrokerMappingStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedBrokerMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCappedBrokerMappingAdjuster(var step: Double) : CappedBrokerMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedBrokerMapping, value: Double): Double = roundTo(value, step)
}

/** Runs the capped broker mapping steps in order and keeps a trace of each. */
data class CappedBrokerMappingEngine(
    var steps: MutableList<CappedBrokerMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedBrokerMapping): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedBrokerMappingStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedBrokerMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedBrokerMappingEngine(flags: Registry): CappedBrokerMappingEngine {
    return CappedBrokerMappingEngine(
        steps = mutableListOf<CappedBrokerMappingStep>(
            FlaggedCappedBrokerMappingAdjuster(flags, "capped-broker-mapping-legacy", 3.8),
            LabelCappedBrokerMappingAdjuster(
                mutableMapOf<String, Double>("retail" to 0.75, "north" to 1.12, "east" to 1.25),
            ),
            MinimumCappedBrokerMappingAdjuster(34.6),
            RoundingCappedBrokerMappingAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 47709.9,
    )
}
