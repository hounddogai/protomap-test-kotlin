package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending cross dock notice calculation. */
interface PendingCrossDockNoticeFactor {
    fun describe(): String
    fun adjust(item: PendingCrossDockNotice, value: Double): Double
}

data class LabelPendingCrossDockNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingCrossDockNoticeFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: PendingCrossDockNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingPendingCrossDockNoticeAdjuster(var step: Double) : PendingCrossDockNoticeFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingCrossDockNotice, value: Double): Double = roundTo(value, step)
}

data class PendingCrossDockNoticeThresholdOffsetAdjuster(var weight: Double) : PendingCrossDockNoticeFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: PendingCrossDockNotice, value: Double): Double = value + item.threshold * weight
}

data class FlaggedPendingCrossDockNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingCrossDockNoticeFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingCrossDockNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the pending cross dock notice steps in order and keeps a trace of each. */
data class PendingCrossDockNoticeEngine(
    var steps: MutableList<PendingCrossDockNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingCrossDockNotice): Double {
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

    fun evaluateAll(items: MutableList<PendingCrossDockNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingCrossDockNoticeEngine(flags: Registry): PendingCrossDockNoticeEngine {
    return PendingCrossDockNoticeEngine(
        steps = mutableListOf<PendingCrossDockNoticeFactor>(
            LabelPendingCrossDockNoticeAdjuster(mutableMapOf<String, Double>("central" to 0.8, "east" to 1.38)),
            RoundingPendingCrossDockNoticeAdjuster(0.25),
            PendingCrossDockNoticeThresholdOffsetAdjuster(1.39),
            FlaggedPendingCrossDockNoticeAdjuster(flags, "pending-cross-dock-notice-adaptive", 8.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 73575.5,
    )
}
