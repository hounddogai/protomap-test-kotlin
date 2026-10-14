package com.acme.transport.network.cutoff.estimate

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the cutoff group calculation. */
interface CutoffGroupFactor {
    fun describe(): String
    fun adjust(item: CutoffGroup, value: Double): Double
}

data class LabelCutoffGroupAdjuster(var factors: MutableMap<String, Double>) : CutoffGroupFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: CutoffGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentCutoffGroupAdjuster(var percent: Double) : CutoffGroupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CutoffGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredCutoffGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CutoffGroupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CutoffGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CutoffGroupEtaMinutesOffsetAdjuster(var weight: Double) : CutoffGroupFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: CutoffGroup, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

/** Runs the cutoff group steps in order and keeps a trace of each. */
data class CutoffGroupEngine(
    var steps: MutableList<CutoffGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CutoffGroup): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CutoffGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCutoffGroupEngine(): CutoffGroupEngine {
    return CutoffGroupEngine(
        steps = mutableListOf<CutoffGroupFactor>(
            LabelCutoffGroupAdjuster(
                mutableMapOf<String, Double>("west" to 0.75, "retail" to 1.18, "south" to 1.28),
            ),
            PercentCutoffGroupAdjuster(18.0),
            TieredCutoffGroupAdjuster(mutableListOf<Double>(2597.0, 2702.0), mutableListOf<Double>(1.06, 1.05)),
            CutoffGroupEtaMinutesOffsetAdjuster(0.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 19613.6,
    )
}
