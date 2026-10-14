package com.acme.transport.customs.broker.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the broker assignment calculation. */
interface BrokerAssignmentStep {
    fun describe(): String
    fun adjust(item: BrokerAssignment, value: Double): Double
}

data class PercentBrokerAssignmentAdjuster(var percent: Double) : BrokerAssignmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredBrokerAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BrokerAssignmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BrokerAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingBrokerAssignmentAdjuster(var step: Double) : BrokerAssignmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerAssignment, value: Double): Double = roundTo(value, step)
}

data class FlaggedBrokerAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BrokerAssignmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BrokerAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class UnitBrokerAssignmentAdjuster(var factors: MutableMap<String, Double>) : BrokerAssignmentStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: BrokerAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the broker assignment steps in order and keeps a trace of each. */
data class BrokerAssignmentEngine(
    var steps: MutableList<BrokerAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BrokerAssignment): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BrokerAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBrokerAssignmentEngine(flags: Registry): BrokerAssignmentEngine {
    return BrokerAssignmentEngine(
        steps = mutableListOf<BrokerAssignmentStep>(
            PercentBrokerAssignmentAdjuster(7.0),
            TieredBrokerAssignmentAdjuster(
                mutableListOf<Double>(1099.0, 1238.0, 1345.0),
                mutableListOf<Double>(1.08, 0.93, 0.85),
            ),
            RoundingBrokerAssignmentAdjuster(5.0),
            FlaggedBrokerAssignmentAdjuster(flags, "broker-assignment-adaptive", 3.3),
            UnitBrokerAssignmentAdjuster(mutableMapOf<String, Double>("south" to 0.93)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 25516.3,
    )
}
