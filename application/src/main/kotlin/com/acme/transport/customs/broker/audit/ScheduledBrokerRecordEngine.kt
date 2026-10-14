package com.acme.transport.customs.broker.audit

import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled broker record calculation. */
interface ScheduledBrokerRecordAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledBrokerRecord, value: Double): Double
}

data class TieredScheduledBrokerRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledBrokerRecordAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledBrokerRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingScheduledBrokerRecordAdjuster(var step: Double) : ScheduledBrokerRecordAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledBrokerRecord, value: Double): Double = roundTo(value, step)
}

data class LabelScheduledBrokerRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledBrokerRecordAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: ScheduledBrokerRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ScheduledBrokerRecordMinimumOffsetAdjuster(var weight: Double) : ScheduledBrokerRecordAdjuster {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: ScheduledBrokerRecord, value: Double): Double = value + item.minimum * weight
}

/** Runs the scheduled broker record steps in order and keeps a trace of each. */
data class ScheduledBrokerRecordEngine(
    var steps: MutableList<ScheduledBrokerRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledBrokerRecord): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledBrokerRecordAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledBrokerRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledBrokerRecordEngine(): ScheduledBrokerRecordEngine {
    return ScheduledBrokerRecordEngine(
        steps = mutableListOf<ScheduledBrokerRecordAdjuster>(
            TieredScheduledBrokerRecordAdjuster(
                mutableListOf<Double>(4319.0, 6781.0, 8008.0, 9992.0),
                mutableListOf<Double>(0.83, 0.96, 0.88, 1.13),
            ),
            RoundingScheduledBrokerRecordAdjuster(1.0),
            LabelScheduledBrokerRecordAdjuster(mutableMapOf<String, Double>("east" to 1.29)),
            ScheduledBrokerRecordMinimumOffsetAdjuster(0.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 60140.2,
    )
}
