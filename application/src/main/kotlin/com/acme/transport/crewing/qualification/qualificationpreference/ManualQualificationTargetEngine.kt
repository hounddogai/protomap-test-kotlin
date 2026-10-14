package com.acme.transport.crewing.qualification.qualificationpreference

/** One step of the manual qualification target calculation. */
interface ManualQualificationTargetAdjuster {
    fun describe(): String
    fun adjust(item: ManualQualificationTarget, value: Double): Double
}

data class ManualQualificationTargetEtaMinutesOffsetAdjuster(
    var weight: Double,
) : ManualQualificationTargetAdjuster {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: ManualQualificationTarget, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class CapManualQualificationTargetAdjuster(var cap: Double) : ManualQualificationTargetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualQualificationTarget, value: Double): Double = minOf(value, cap)
}

data class LabelManualQualificationTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualQualificationTargetAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: ManualQualificationTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual qualification target steps in order and keeps a trace of each. */
data class ManualQualificationTargetEngine(
    var steps: MutableList<ManualQualificationTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualQualificationTarget): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualQualificationTargetAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualQualificationTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualQualificationTargetEngine(): ManualQualificationTargetEngine {
    return ManualQualificationTargetEngine(
        steps = mutableListOf<ManualQualificationTargetAdjuster>(
            ManualQualificationTargetEtaMinutesOffsetAdjuster(1.94),
            CapManualQualificationTargetAdjuster(9231.0),
            LabelManualQualificationTargetAdjuster(
                mutableMapOf<String, Double>("central" to 1.33, "north" to 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 99748.7,
    )
}
