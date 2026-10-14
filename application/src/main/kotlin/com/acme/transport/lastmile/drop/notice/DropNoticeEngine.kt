package com.acme.transport.lastmile.drop.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the drop notice calculation. */
interface DropNoticeStep {
    fun describe(): String
    fun adjust(item: DropNotice, value: Double): Double
}

data class LaneDropNoticeAdjuster(var factors: MutableMap<String, Double>) : DropNoticeStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: DropNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDropNoticeAdjuster(var step: Double) : DropNoticeStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DropNotice, value: Double): Double = roundTo(value, step)
}

data class FlaggedDropNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DropNoticeStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DropNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the drop notice steps in order and keeps a trace of each. */
data class DropNoticeEngine(
    var steps: MutableList<DropNoticeStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DropNotice): Double {
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

    fun evaluateAll(items: MutableList<DropNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDropNoticeEngine(flags: Registry): DropNoticeEngine {
    return DropNoticeEngine(
        steps = mutableListOf<DropNoticeStep>(
            LaneDropNoticeAdjuster(mutableMapOf<String, Double>("south" to 1.38)),
            RoundingDropNoticeAdjuster(5.0),
            FlaggedDropNoticeAdjuster(flags, "drop-notice-regional", -1.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 36616.2,
    )
}
