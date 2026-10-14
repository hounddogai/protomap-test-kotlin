package com.acme.transport.lastmile.redelivery.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default redelivery notice calculation. */
interface DefaultRedeliveryNoticeAdjuster {
    fun describe(): String
    fun adjust(item: DefaultRedeliveryNotice, value: Double): Double
}

data class CapDefaultRedeliveryNoticeAdjuster(var cap: Double) : DefaultRedeliveryNoticeAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultRedeliveryNotice, value: Double): Double = minOf(value, cap)
}

data class FlaggedDefaultRedeliveryNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultRedeliveryNoticeAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultRedeliveryNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDefaultRedeliveryNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultRedeliveryNoticeAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultRedeliveryNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the default redelivery notice steps in order and keeps a trace of each. */
data class DefaultRedeliveryNoticeEngine(
    var steps: MutableList<DefaultRedeliveryNoticeAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultRedeliveryNotice): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultRedeliveryNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultRedeliveryNoticeEngine(flags: Registry): DefaultRedeliveryNoticeEngine {
    return DefaultRedeliveryNoticeEngine(
        steps = mutableListOf<DefaultRedeliveryNoticeAdjuster>(
            CapDefaultRedeliveryNoticeAdjuster(13112.0),
            FlaggedDefaultRedeliveryNoticeAdjuster(flags, "default-redelivery-notice-rolling", -9.6),
            TieredDefaultRedeliveryNoticeAdjuster(
                mutableListOf<Double>(712.0, 8383.0, 9745.0),
                mutableListOf<Double>(1.1, 1.12, 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 51872.2,
    )
}
