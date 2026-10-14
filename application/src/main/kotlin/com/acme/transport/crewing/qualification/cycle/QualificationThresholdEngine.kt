package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the qualification threshold calculation. */
interface QualificationThresholdFactor {
    fun describe(): String
    fun adjust(item: QualificationThreshold, value: Double): Double
}

data class LaneQualificationThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : QualificationThresholdFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: QualificationThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapQualificationThresholdAdjuster(var cap: Double) : QualificationThresholdFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: QualificationThreshold, value: Double): Double = minOf(value, cap)
}

data class PercentQualificationThresholdAdjuster(var percent: Double) : QualificationThresholdFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedQualificationThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : QualificationThresholdFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: QualificationThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class QualificationThresholdAttemptsOffsetAdjuster(var weight: Double) : QualificationThresholdFactor {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: QualificationThreshold, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class RoundingQualificationThresholdAdjuster(var step: Double) : QualificationThresholdFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationThreshold, value: Double): Double = roundTo(value, step)
}

/** Runs the qualification threshold steps in order and keeps a trace of each. */
data class QualificationThresholdEngine(
    var steps: MutableList<QualificationThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: QualificationThreshold): Double {
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

    fun evaluateAll(items: MutableList<QualificationThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultQualificationThresholdEngine(flags: Registry): QualificationThresholdEngine {
    return QualificationThresholdEngine(
        steps = mutableListOf<QualificationThresholdFactor>(
            LaneQualificationThresholdAdjuster(mutableMapOf<String, Double>("east" to 0.84)),
            CapQualificationThresholdAdjuster(26918.0),
            PercentQualificationThresholdAdjuster(-6.4),
            FlaggedQualificationThresholdAdjuster(flags, "qualification-threshold-tiered", 3.3),
            QualificationThresholdAttemptsOffsetAdjuster(1.62),
            RoundingQualificationThresholdAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 51714.3,
    )
}
