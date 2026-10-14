package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the qualification segment calculation. */
interface QualificationSegmentAdjuster {
    fun describe(): String
    fun adjust(item: QualificationSegment, value: Double): Double
}

data class QualificationSegmentMaximumOffsetAdjuster(var weight: Double) : QualificationSegmentAdjuster {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: QualificationSegment, value: Double): Double = value + item.maximum * weight
}

data class FlaggedQualificationSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : QualificationSegmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: QualificationSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingQualificationSegmentAdjuster(var step: Double) : QualificationSegmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationSegment, value: Double): Double = roundTo(value, step)
}

data class PercentQualificationSegmentAdjuster(var percent: Double) : QualificationSegmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the qualification segment steps in order and keeps a trace of each. */
data class QualificationSegmentEngine(
    var steps: MutableList<QualificationSegmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: QualificationSegment): Double {
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

    fun evaluateAll(items: MutableList<QualificationSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultQualificationSegmentEngine(flags: Registry): QualificationSegmentEngine {
    return QualificationSegmentEngine(
        steps = mutableListOf<QualificationSegmentAdjuster>(
            QualificationSegmentMaximumOffsetAdjuster(1.7),
            FlaggedQualificationSegmentAdjuster(flags, "qualification-segment-fallback", 7.7),
            RoundingQualificationSegmentAdjuster(0.05),
            PercentQualificationSegmentAdjuster(23.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 87398.4,
    )
}
