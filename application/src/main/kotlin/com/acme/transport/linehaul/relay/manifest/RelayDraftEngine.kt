package com.acme.transport.linehaul.relay.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the relay draft calculation. */
interface RelayDraftAdjuster {
    fun describe(): String
    fun adjust(item: RelayDraft, value: Double): Double
}

data class CapRelayDraftAdjuster(var cap: Double) : RelayDraftAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RelayDraft, value: Double): Double = minOf(value, cap)
}

data class PercentRelayDraftAdjuster(var percent: Double) : RelayDraftAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelayDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRelayDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayDraftAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleRelayDraftAdjuster(var factors: MutableMap<String, Double>) : RelayDraftAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: RelayDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRelayDraftAdjuster(var step: Double) : RelayDraftAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayDraft, value: Double): Double = roundTo(value, step)
}

data class FlaggedRelayDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelayDraftAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelayDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the relay draft steps in order and keeps a trace of each. */
data class RelayDraftEngine(
    var steps: MutableList<RelayDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RelayDraft): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RelayDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRelayDraftEngine(flags: Registry): RelayDraftEngine {
    return RelayDraftEngine(
        steps = mutableListOf<RelayDraftAdjuster>(
            CapRelayDraftAdjuster(33150.0),
            PercentRelayDraftAdjuster(-3.2),
            TieredRelayDraftAdjuster(
                mutableListOf<Double>(3965.0, 6277.0, 8491.0, 9452.0),
                mutableListOf<Double>(0.91, 0.81, 0.86, 1.17),
            ),
            TitleRelayDraftAdjuster(mutableMapOf<String, Double>("south" to 1.35, "north" to 0.75)),
            RoundingRelayDraftAdjuster(5.0),
            FlaggedRelayDraftAdjuster(flags, "relay-draft-regional", -9.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 30604.0,
    )
}
