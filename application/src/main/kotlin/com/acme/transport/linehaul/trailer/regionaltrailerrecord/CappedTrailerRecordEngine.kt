package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped trailer record calculation. */
interface CappedTrailerRecordAdjuster {
    fun describe(): String
    fun adjust(item: CappedTrailerRecord, value: Double): Double
}

data class LabelCappedTrailerRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedTrailerRecordAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: CappedTrailerRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCappedTrailerRecordAdjuster(var step: Double) : CappedTrailerRecordAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedTrailerRecord, value: Double): Double = roundTo(value, step)
}

data class CapCappedTrailerRecordAdjuster(var cap: Double) : CappedTrailerRecordAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedTrailerRecord, value: Double): Double = minOf(value, cap)
}

data class TieredCappedTrailerRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedTrailerRecordAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedTrailerRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped trailer record steps in order and keeps a trace of each. */
data class CappedTrailerRecordEngine(
    var steps: MutableList<CappedTrailerRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedTrailerRecord): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedTrailerRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedTrailerRecordEngine(): CappedTrailerRecordEngine {
    return CappedTrailerRecordEngine(
        steps = mutableListOf<CappedTrailerRecordAdjuster>(
            LabelCappedTrailerRecordAdjuster(mutableMapOf<String, Double>("central" to 1.0)),
            RoundingCappedTrailerRecordAdjuster(0.5),
            CapCappedTrailerRecordAdjuster(31920.0),
            TieredCappedTrailerRecordAdjuster(
                mutableListOf<Double>(3256.0, 5128.0, 9117.0),
                mutableListOf<Double>(1.07, 1.03, 1.1),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 70898.0,
    )
}
