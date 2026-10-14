package com.acme.transport.emissions.evcharge.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the ev charge notice calculation. */
interface EvChargeNoticeModifier {
    fun describe(): String
    fun adjust(item: EvChargeNotice, value: Double): Double
}

data class TieredEvChargeNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeNoticeModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedEvChargeNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeNoticeModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class EvChargeNoticeCeilingOffsetAdjuster(var weight: Double) : EvChargeNoticeModifier {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: EvChargeNotice, value: Double): Double = value + item.ceiling * weight
}

data class PercentEvChargeNoticeAdjuster(var percent: Double) : EvChargeNoticeModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EvChargeNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the ev charge notice steps in order and keeps a trace of each. */
data class EvChargeNoticeEngine(
    var steps: MutableList<EvChargeNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: EvChargeNotice): Double {
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

    fun evaluateAll(items: MutableList<EvChargeNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultEvChargeNoticeEngine(flags: Registry): EvChargeNoticeEngine {
    return EvChargeNoticeEngine(
        steps = mutableListOf<EvChargeNoticeModifier>(
            TieredEvChargeNoticeAdjuster(
                mutableListOf<Double>(870.0, 3740.0, 7361.0),
                mutableListOf<Double>(1.13, 1.12, 0.93),
            ),
            FlaggedEvChargeNoticeAdjuster(flags, "ev-charge-notice-capped", 7.0),
            EvChargeNoticeCeilingOffsetAdjuster(1.44),
            PercentEvChargeNoticeAdjuster(-19.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 54798.9,
    )
}
