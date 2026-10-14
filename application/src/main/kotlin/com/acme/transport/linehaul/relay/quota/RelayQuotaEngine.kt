package com.acme.transport.linehaul.relay.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the relay quota calculation. */
interface RelayQuotaModifier {
    fun describe(): String
    fun adjust(item: RelayQuota, value: Double): Double
}

data class MinimumRelayQuotaAdjuster(var minimum: Double) : RelayQuotaModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RelayQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRelayQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayQuotaModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RelayQuotaExpiresAtOffsetAdjuster(var weight: Double) : RelayQuotaModifier {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: RelayQuota, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class FlaggedRelayQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelayQuotaModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelayQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRelayQuotaAdjuster(var cap: Double) : RelayQuotaModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RelayQuota, value: Double): Double = minOf(value, cap)
}

/** Runs the relay quota steps in order and keeps a trace of each. */
data class RelayQuotaEngine(
    var steps: MutableList<RelayQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RelayQuota): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RelayQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRelayQuotaEngine(flags: Registry): RelayQuotaEngine {
    return RelayQuotaEngine(
        steps = mutableListOf<RelayQuotaModifier>(
            MinimumRelayQuotaAdjuster(3.8),
            TieredRelayQuotaAdjuster(mutableListOf<Double>(4441.0, 5341.0), mutableListOf<Double>(0.81, 1.16)),
            RelayQuotaExpiresAtOffsetAdjuster(1.92),
            FlaggedRelayQuotaAdjuster(flags, "relay-quota-lenient", 1.3),
            CapRelayQuotaAdjuster(43708.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 70868.6,
    )
}
