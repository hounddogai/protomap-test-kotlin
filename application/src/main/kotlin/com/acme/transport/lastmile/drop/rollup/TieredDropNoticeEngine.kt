package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered drop notice calculation. */
interface TieredDropNoticeFactor {
    fun describe(): String
    fun adjust(item: TieredDropNotice, value: Double): Double
}

data class MinimumTieredDropNoticeAdjuster(var minimum: Double) : TieredDropNoticeFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredDropNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentTieredDropNoticeAdjuster(var percent: Double) : TieredDropNoticeFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredDropNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDropNoticeScoreOffsetAdjuster(var weight: Double) : TieredDropNoticeFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: TieredDropNotice, value: Double): Double = value + item.score * weight
}

/** Runs the tiered drop notice steps in order and keeps a trace of each. */
data class TieredDropNoticeEngine(
    var steps: MutableList<TieredDropNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredDropNotice): Double {
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

    fun evaluateAll(items: MutableList<TieredDropNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredDropNoticeEngine(): TieredDropNoticeEngine {
    return TieredDropNoticeEngine(
        steps = mutableListOf<TieredDropNoticeFactor>(
            MinimumTieredDropNoticeAdjuster(15.9),
            PercentTieredDropNoticeAdjuster(-12.9),
            TieredDropNoticeScoreOffsetAdjuster(1.93),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 78254.6,
    )
}
