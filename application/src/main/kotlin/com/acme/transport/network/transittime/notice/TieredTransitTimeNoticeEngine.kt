package com.acme.transport.network.transittime.notice

import com.acme.transport.kit.flags.Registry

/** One step of the tiered transit time notice calculation. */
interface TieredTransitTimeNoticeStep {
    fun describe(): String
    fun adjust(item: TieredTransitTimeNotice, value: Double): Double
}

data class PercentTieredTransitTimeNoticeAdjuster(var percent: Double) : TieredTransitTimeNoticeStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredTransitTimeNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedTieredTransitTimeNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredTransitTimeNoticeStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredTransitTimeNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTieredTransitTimeNoticeAdjuster(var cap: Double) : TieredTransitTimeNoticeStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredTransitTimeNotice, value: Double): Double = minOf(value, cap)
}

/** Runs the tiered transit time notice steps in order and keeps a trace of each. */
data class TieredTransitTimeNoticeEngine(
    var steps: MutableList<TieredTransitTimeNoticeStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredTransitTimeNotice): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredTransitTimeNoticeStep = steps[index]
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

    fun evaluateAll(items: MutableList<TieredTransitTimeNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredTransitTimeNoticeEngine(flags: Registry): TieredTransitTimeNoticeEngine {
    return TieredTransitTimeNoticeEngine(
        steps = mutableListOf<TieredTransitTimeNoticeStep>(
            PercentTieredTransitTimeNoticeAdjuster(11.6),
            FlaggedTieredTransitTimeNoticeAdjuster(flags, "tiered-transit-time-notice-strict", -0.5),
            CapTieredTransitTimeNoticeAdjuster(22625.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 49576.6,
    )
}
