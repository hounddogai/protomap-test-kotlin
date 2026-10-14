package com.acme.transport.crewing.qualification.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the qualification allocation calculation. */
interface QualificationAllocationStep {
    fun describe(): String
    fun adjust(item: QualificationAllocation, value: Double): Double
}

data class TieredQualificationAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : QualificationAllocationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: QualificationAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumQualificationAllocationAdjuster(var minimum: Double) : QualificationAllocationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: QualificationAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingQualificationAllocationAdjuster(var step: Double) : QualificationAllocationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationAllocation, value: Double): Double = roundTo(value, step)
}

data class CapQualificationAllocationAdjuster(var cap: Double) : QualificationAllocationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: QualificationAllocation, value: Double): Double = minOf(value, cap)
}

data class PercentQualificationAllocationAdjuster(var percent: Double) : QualificationAllocationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedQualificationAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : QualificationAllocationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: QualificationAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the qualification allocation steps in order and keeps a trace of each. */
data class QualificationAllocationEngine(
    var steps: MutableList<QualificationAllocationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: QualificationAllocation): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<QualificationAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultQualificationAllocationEngine(flags: Registry): QualificationAllocationEngine {
    return QualificationAllocationEngine(
        steps = mutableListOf<QualificationAllocationStep>(
            TieredQualificationAllocationAdjuster(
                mutableListOf<Double>(2631.0, 9577.0),
                mutableListOf<Double>(0.99, 1.17),
            ),
            MinimumQualificationAllocationAdjuster(8.8),
            RoundingQualificationAllocationAdjuster(0.5),
            CapQualificationAllocationAdjuster(46215.0),
            PercentQualificationAllocationAdjuster(2.9),
            FlaggedQualificationAllocationAdjuster(flags, "qualification-allocation-lenient", 5.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 32787.5,
    )
}
