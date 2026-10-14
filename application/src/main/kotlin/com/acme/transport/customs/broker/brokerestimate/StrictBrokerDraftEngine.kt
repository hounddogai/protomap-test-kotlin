package com.acme.transport.customs.broker.brokerestimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict broker draft calculation. */
interface StrictBrokerDraftStep {
    fun describe(): String
    fun adjust(item: StrictBrokerDraft, value: Double): Double
}

data class MinimumStrictBrokerDraftAdjuster(var minimum: Double) : StrictBrokerDraftStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictBrokerDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredStrictBrokerDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictBrokerDraftStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictBrokerDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedStrictBrokerDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictBrokerDraftStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictBrokerDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ChannelStrictBrokerDraftAdjuster(var factors: MutableMap<String, Double>) : StrictBrokerDraftStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: StrictBrokerDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingStrictBrokerDraftAdjuster(var step: Double) : StrictBrokerDraftStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictBrokerDraft, value: Double): Double = roundTo(value, step)
}

data class StrictBrokerDraftAmountOffsetAdjuster(var weight: Double) : StrictBrokerDraftStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: StrictBrokerDraft, value: Double): Double = value + item.amount * weight
}

/** Runs the strict broker draft steps in order and keeps a trace of each. */
data class StrictBrokerDraftEngine(
    var steps: MutableList<StrictBrokerDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictBrokerDraft): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictBrokerDraftStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictBrokerDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictBrokerDraftEngine(flags: Registry): StrictBrokerDraftEngine {
    return StrictBrokerDraftEngine(
        steps = mutableListOf<StrictBrokerDraftStep>(
            MinimumStrictBrokerDraftAdjuster(23.2),
            TieredStrictBrokerDraftAdjuster(
                mutableListOf<Double>(51.0, 3927.0, 8244.0),
                mutableListOf<Double>(1.05, 0.99, 1.16),
            ),
            FlaggedStrictBrokerDraftAdjuster(flags, "strict-broker-draft-regional", 5.6),
            ChannelStrictBrokerDraftAdjuster(
                mutableMapOf<String, Double>("retail" to 1.24, "online" to 1.36, "west" to 0.92),
            ),
            RoundingStrictBrokerDraftAdjuster(0.25),
            StrictBrokerDraftAmountOffsetAdjuster(1.15),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 98116.7,
    )
}
