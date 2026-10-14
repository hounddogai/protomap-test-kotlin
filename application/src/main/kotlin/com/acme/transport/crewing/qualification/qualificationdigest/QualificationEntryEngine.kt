package com.acme.transport.crewing.qualification.qualificationdigest

/** One step of the qualification entry calculation. */
interface QualificationEntryAdjuster {
    fun describe(): String
    fun adjust(item: QualificationEntry, value: Double): Double
}

data class TieredQualificationEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : QualificationEntryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: QualificationEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class QualificationEntryExpiresAtOffsetAdjuster(var weight: Double) : QualificationEntryAdjuster {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: QualificationEntry, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class CategoryQualificationEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : QualificationEntryAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: QualificationEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumQualificationEntryAdjuster(var minimum: Double) : QualificationEntryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: QualificationEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the qualification entry steps in order and keeps a trace of each. */
data class QualificationEntryEngine(
    var steps: MutableList<QualificationEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: QualificationEntry): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: QualificationEntryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<QualificationEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultQualificationEntryEngine(): QualificationEntryEngine {
    return QualificationEntryEngine(
        steps = mutableListOf<QualificationEntryAdjuster>(
            TieredQualificationEntryAdjuster(
                mutableListOf<Double>(1651.0, 5265.0),
                mutableListOf<Double>(0.86, 1.14),
            ),
            QualificationEntryExpiresAtOffsetAdjuster(0.13),
            CategoryQualificationEntryAdjuster(
                mutableMapOf<String, Double>("east" to 1.36, "west" to 0.93, "online" to 1.31),
            ),
            MinimumQualificationEntryAdjuster(34.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 43164.5,
    )
}
