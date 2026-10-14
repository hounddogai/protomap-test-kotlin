package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the relay ledger calculation. */
interface RelayLedgerModifier {
    fun describe(): String
    fun adjust(item: RelayLedger, value: Double): Double
}

data class MinimumRelayLedgerAdjuster(var minimum: Double) : RelayLedgerModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RelayLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRelayLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelayLedgerModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelayLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryRelayLedgerAdjuster(var factors: MutableMap<String, Double>) : RelayLedgerModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: RelayLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRelayLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayLedgerModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRelayLedgerAdjuster(var step: Double) : RelayLedgerModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayLedger, value: Double): Double = roundTo(value, step)
}

/** Runs the relay ledger steps in order and keeps a trace of each. */
data class RelayLedgerEngine(
    var steps: MutableList<RelayLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RelayLedger): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RelayLedgerModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RelayLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRelayLedgerEngine(flags: Registry): RelayLedgerEngine {
    return RelayLedgerEngine(
        steps = mutableListOf<RelayLedgerModifier>(
            MinimumRelayLedgerAdjuster(42.0),
            FlaggedRelayLedgerAdjuster(flags, "relay-ledger-tiered", -7.4),
            CategoryRelayLedgerAdjuster(
                mutableMapOf<String, Double>("west" to 1.1, "central" to 1.37, "retail" to 0.99),
            ),
            TieredRelayLedgerAdjuster(
                mutableListOf<Double>(3150.0, 6124.0, 8898.0),
                mutableListOf<Double>(1.04, 1.19, 0.82),
            ),
            RoundingRelayLedgerAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 67769.6,
    )
}
