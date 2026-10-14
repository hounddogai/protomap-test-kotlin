package com.acme.transport.customs.hold.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the hold notice calculation. */
interface HoldNoticeModifier {
    fun describe(): String
    fun adjust(item: HoldNotice, value: Double): Double
}

data class PercentHoldNoticeAdjuster(var percent: Double) : HoldNoticeModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HoldNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneHoldNoticeAdjuster(var factors: MutableMap<String, Double>) : HoldNoticeModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: HoldNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedHoldNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HoldNoticeModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HoldNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumHoldNoticeAdjuster(var minimum: Double) : HoldNoticeModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HoldNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the hold notice steps in order and keeps a trace of each. */
data class HoldNoticeEngine(
    var steps: MutableList<HoldNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: HoldNotice): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<HoldNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultHoldNoticeEngine(flags: Registry): HoldNoticeEngine {
    return HoldNoticeEngine(
        steps = mutableListOf<HoldNoticeModifier>(
            PercentHoldNoticeAdjuster(5.1),
            LaneHoldNoticeAdjuster(mutableMapOf<String, Double>("south" to 0.76)),
            FlaggedHoldNoticeAdjuster(flags, "hold-notice-pending", -1.3),
            MinimumHoldNoticeAdjuster(43.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 76066.4,
    )
}
