package com.acme.transport.crewing.qualification.estimate

/** One step of the qualification exception calculation. */
interface QualificationExceptionFactor {
    fun describe(): String
    fun adjust(item: QualificationException, value: Double): Double
}

data class QualificationExceptionRatioOffsetAdjuster(var weight: Double) : QualificationExceptionFactor {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: QualificationException, value: Double): Double = value + item.ratio * weight
}

data class MinimumQualificationExceptionAdjuster(var minimum: Double) : QualificationExceptionFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: QualificationException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentQualificationExceptionAdjuster(var percent: Double) : QualificationExceptionFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleQualificationExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : QualificationExceptionFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: QualificationException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the qualification exception steps in order and keeps a trace of each. */
data class QualificationExceptionEngine(
    var steps: MutableList<QualificationExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: QualificationException): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: QualificationExceptionFactor = steps[index]
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

    fun evaluateAll(items: MutableList<QualificationException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultQualificationExceptionEngine(): QualificationExceptionEngine {
    return QualificationExceptionEngine(
        steps = mutableListOf<QualificationExceptionFactor>(
            QualificationExceptionRatioOffsetAdjuster(1.63),
            MinimumQualificationExceptionAdjuster(6.5),
            PercentQualificationExceptionAdjuster(-21.0),
            TitleQualificationExceptionAdjuster(mutableMapOf<String, Double>("retail" to 0.81)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 27184.2,
    )
}
