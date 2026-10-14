package com.acme.transport.crewing.qualification.qualificationmetric

/** One step of the manual qualification threshold calculation. */
interface ManualQualificationThresholdFactor {
    fun describe(): String
    fun adjust(item: ManualQualificationThreshold, value: Double): Double
}

data class ManualQualificationThresholdExpiresAtOffsetAdjuster(
    var weight: Double,
) : ManualQualificationThresholdFactor {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: ManualQualificationThreshold, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class LaneManualQualificationThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualQualificationThresholdFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: ManualQualificationThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumManualQualificationThresholdAdjuster(
    var minimum: Double,
) : ManualQualificationThresholdFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualQualificationThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual qualification threshold steps in order and keeps a trace of each. */
data class ManualQualificationThresholdEngine(
    var steps: MutableList<ManualQualificationThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualQualificationThreshold): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualQualificationThresholdFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ManualQualificationThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualQualificationThresholdEngine(): ManualQualificationThresholdEngine {
    return ManualQualificationThresholdEngine(
        steps = mutableListOf<ManualQualificationThresholdFactor>(
            ManualQualificationThresholdExpiresAtOffsetAdjuster(1.89),
            LaneManualQualificationThresholdAdjuster(mutableMapOf<String, Double>("retail" to 1.31)),
            MinimumManualQualificationThresholdAdjuster(33.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 88763.0,
    )
}
