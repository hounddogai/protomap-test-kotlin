package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the broker manifest calculation. */
interface BrokerManifestAdjuster {
    fun describe(): String
    fun adjust(item: BrokerManifest, value: Double): Double
}

data class BrokerManifestBudgetOffsetAdjuster(var weight: Double) : BrokerManifestAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: BrokerManifest, value: Double): Double = value + item.budget * weight
}

data class NameBrokerManifestAdjuster(var factors: MutableMap<String, Double>) : BrokerManifestAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: BrokerManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumBrokerManifestAdjuster(var minimum: Double) : BrokerManifestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BrokerManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentBrokerManifestAdjuster(var percent: Double) : BrokerManifestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingBrokerManifestAdjuster(var step: Double) : BrokerManifestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerManifest, value: Double): Double = roundTo(value, step)
}

data class FlaggedBrokerManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BrokerManifestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BrokerManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the broker manifest steps in order and keeps a trace of each. */
data class BrokerManifestEngine(
    var steps: MutableList<BrokerManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BrokerManifest): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BrokerManifestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BrokerManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBrokerManifestEngine(flags: Registry): BrokerManifestEngine {
    return BrokerManifestEngine(
        steps = mutableListOf<BrokerManifestAdjuster>(
            BrokerManifestBudgetOffsetAdjuster(1.75),
            NameBrokerManifestAdjuster(
                mutableMapOf<String, Double>("south" to 1.31, "north" to 1.37, "east" to 0.81),
            ),
            MinimumBrokerManifestAdjuster(1.9),
            PercentBrokerManifestAdjuster(5.1),
            RoundingBrokerManifestAdjuster(0.1),
            FlaggedBrokerManifestAdjuster(flags, "broker-manifest-primary", 0.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 88234.5,
    )
}
