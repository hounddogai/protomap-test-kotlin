package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the qualification window calculation. */
interface QualificationWindowFactor {
    fun describe(): String
    fun adjust(item: QualificationWindow, value: Double): Double
}

data class RoundingQualificationWindowAdjuster(var step: Double) : QualificationWindowFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationWindow, value: Double): Double = roundTo(value, step)
}

data class QualificationWindowSequenceOffsetAdjuster(var weight: Double) : QualificationWindowFactor {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: QualificationWindow, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class UnitQualificationWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : QualificationWindowFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: QualificationWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumQualificationWindowAdjuster(var minimum: Double) : QualificationWindowFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: QualificationWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the qualification window steps in order and keeps a trace of each. */
data class QualificationWindowEngine(
    var steps: MutableList<QualificationWindowFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: QualificationWindow): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<QualificationWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultQualificationWindowEngine(): QualificationWindowEngine {
    return QualificationWindowEngine(
        steps = mutableListOf<QualificationWindowFactor>(
            RoundingQualificationWindowAdjuster(1.0),
            QualificationWindowSequenceOffsetAdjuster(1.49),
            UnitQualificationWindowAdjuster(mutableMapOf<String, Double>("west" to 1.15)),
            MinimumQualificationWindowAdjuster(5.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 30042.7,
    )
}
