package com.acme.transport.crewing.qualification.qualificationoverride

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the qualification profile calculation. */
interface QualificationProfileFactor {
    fun describe(): String
    fun adjust(item: QualificationProfile, value: Double): Double
}

data class RoundingQualificationProfileAdjuster(var step: Double) : QualificationProfileFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationProfile, value: Double): Double = roundTo(value, step)
}

data class MinimumQualificationProfileAdjuster(var minimum: Double) : QualificationProfileFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: QualificationProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapQualificationProfileAdjuster(var cap: Double) : QualificationProfileFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: QualificationProfile, value: Double): Double = minOf(value, cap)
}

data class CountryQualificationProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : QualificationProfileFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: QualificationProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentQualificationProfileAdjuster(var percent: Double) : QualificationProfileFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class QualificationProfileScoreOffsetAdjuster(var weight: Double) : QualificationProfileFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: QualificationProfile, value: Double): Double = value + item.score * weight
}

/** Runs the qualification profile steps in order and keeps a trace of each. */
data class QualificationProfileEngine(
    var steps: MutableList<QualificationProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: QualificationProfile): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<QualificationProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultQualificationProfileEngine(): QualificationProfileEngine {
    return QualificationProfileEngine(
        steps = mutableListOf<QualificationProfileFactor>(
            RoundingQualificationProfileAdjuster(0.1),
            MinimumQualificationProfileAdjuster(20.7),
            CapQualificationProfileAdjuster(559.0),
            CountryQualificationProfileAdjuster(
                mutableMapOf<String, Double>("north" to 0.84, "online" to 1.07, "south" to 0.85),
            ),
            PercentQualificationProfileAdjuster(-10.1),
            QualificationProfileScoreOffsetAdjuster(0.58),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 35522.4,
    )
}
