package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the relay notice calculation. */
interface RelayNoticeAdjuster {
    fun describe(): String
    fun adjust(item: RelayNotice, value: Double): Double
}

data class RelayNoticeOdometerOffsetAdjuster(var weight: Double) : RelayNoticeAdjuster {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: RelayNotice, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class TieredRelayNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayNoticeAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRelayNoticeAdjuster(var percent: Double) : RelayNoticeAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelayNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRelayNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelayNoticeAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelayNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRelayNoticeAdjuster(var cap: Double) : RelayNoticeAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RelayNotice, value: Double): Double = minOf(value, cap)
}

data class LocaleRelayNoticeAdjuster(var factors: MutableMap<String, Double>) : RelayNoticeAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: RelayNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the relay notice steps in order and keeps a trace of each. */
data class RelayNoticeEngine(
    var steps: MutableList<RelayNoticeAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RelayNotice): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RelayNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRelayNoticeEngine(flags: Registry): RelayNoticeEngine {
    return RelayNoticeEngine(
        steps = mutableListOf<RelayNoticeAdjuster>(
            RelayNoticeOdometerOffsetAdjuster(0.17),
            TieredRelayNoticeAdjuster(
                mutableListOf<Double>(287.0, 3141.0, 3282.0, 8088.0),
                mutableListOf<Double>(1.18, 0.97, 0.83, 1.19),
            ),
            PercentRelayNoticeAdjuster(3.9),
            FlaggedRelayNoticeAdjuster(flags, "relay-notice-manual", 3.1),
            CapRelayNoticeAdjuster(21918.0),
            LocaleRelayNoticeAdjuster(mutableMapOf<String, Double>("north" to 1.18)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 69409.8,
    )
}
