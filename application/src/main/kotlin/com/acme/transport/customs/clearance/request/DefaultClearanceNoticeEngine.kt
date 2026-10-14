package com.acme.transport.customs.clearance.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default clearance notice calculation. */
interface DefaultClearanceNoticeModifier {
    fun describe(): String
    fun adjust(item: DefaultClearanceNotice, value: Double): Double
}

data class RoundingDefaultClearanceNoticeAdjuster(var step: Double) : DefaultClearanceNoticeModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultClearanceNotice, value: Double): Double = roundTo(value, step)
}

data class PercentDefaultClearanceNoticeAdjuster(var percent: Double) : DefaultClearanceNoticeModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultClearanceNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDefaultClearanceNoticeAdjuster(var cap: Double) : DefaultClearanceNoticeModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultClearanceNotice, value: Double): Double = minOf(value, cap)
}

data class CategoryDefaultClearanceNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultClearanceNoticeModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: DefaultClearanceNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDefaultClearanceNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultClearanceNoticeModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultClearanceNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the default clearance notice steps in order and keeps a trace of each. */
data class DefaultClearanceNoticeEngine(
    var steps: MutableList<DefaultClearanceNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultClearanceNotice): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultClearanceNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultClearanceNoticeEngine(flags: Registry): DefaultClearanceNoticeEngine {
    return DefaultClearanceNoticeEngine(
        steps = mutableListOf<DefaultClearanceNoticeModifier>(
            RoundingDefaultClearanceNoticeAdjuster(5.0),
            PercentDefaultClearanceNoticeAdjuster(-19.2),
            CapDefaultClearanceNoticeAdjuster(46702.0),
            CategoryDefaultClearanceNoticeAdjuster(
                mutableMapOf<String, Double>("west" to 1.18, "central" to 0.91),
            ),
            FlaggedDefaultClearanceNoticeAdjuster(flags, "default-clearance-notice-regional", 7.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 15370.2,
    )
}
