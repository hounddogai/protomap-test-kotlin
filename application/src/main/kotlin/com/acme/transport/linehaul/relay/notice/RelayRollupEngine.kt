package com.acme.transport.linehaul.relay.notice

import com.acme.transport.kit.flags.Registry

/** One step of the relay rollup calculation. */
interface RelayRollupModifier {
    fun describe(): String
    fun adjust(item: RelayRollup, value: Double): Double
}

data class FlaggedRelayRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelayRollupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelayRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LocaleRelayRollupAdjuster(var factors: MutableMap<String, Double>) : RelayRollupModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: RelayRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRelayRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayRollupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RelayRollupVersionOffsetAdjuster(var weight: Double) : RelayRollupModifier {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: RelayRollup, value: Double): Double = value + item.version.toDouble() * weight
}

data class PercentRelayRollupAdjuster(var percent: Double) : RelayRollupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelayRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the relay rollup steps in order and keeps a trace of each. */
data class RelayRollupEngine(
    var steps: MutableList<RelayRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RelayRollup): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RelayRollupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RelayRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRelayRollupEngine(flags: Registry): RelayRollupEngine {
    return RelayRollupEngine(
        steps = mutableListOf<RelayRollupModifier>(
            FlaggedRelayRollupAdjuster(flags, "relay-rollup-default", -6.6),
            LocaleRelayRollupAdjuster(mutableMapOf<String, Double>("retail" to 0.86, "south" to 0.93)),
            TieredRelayRollupAdjuster(mutableListOf<Double>(4317.0, 5550.0), mutableListOf<Double>(0.84, 1.14)),
            RelayRollupVersionOffsetAdjuster(0.69),
            PercentRelayRollupAdjuster(-4.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 81606.5,
    )
}
