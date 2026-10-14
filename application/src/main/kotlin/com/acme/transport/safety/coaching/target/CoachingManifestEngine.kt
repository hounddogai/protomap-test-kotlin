package com.acme.transport.safety.coaching.target

/** One step of the coaching manifest calculation. */
interface CoachingManifestAdjuster {
    fun describe(): String
    fun adjust(item: CoachingManifest, value: Double): Double
}

data class PercentCoachingManifestAdjuster(var percent: Double) : CoachingManifestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CoachingManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCoachingManifestAdjuster(var cap: Double) : CoachingManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CoachingManifest, value: Double): Double = minOf(value, cap)
}

data class CoachingManifestQuantityOffsetAdjuster(var weight: Double) : CoachingManifestAdjuster {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: CoachingManifest, value: Double): Double = value + item.quantity.toDouble() * weight
}

/** Runs the coaching manifest steps in order and keeps a trace of each. */
data class CoachingManifestEngine(
    var steps: MutableList<CoachingManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CoachingManifest): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CoachingManifestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CoachingManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCoachingManifestEngine(): CoachingManifestEngine {
    return CoachingManifestEngine(
        steps = mutableListOf<CoachingManifestAdjuster>(
            PercentCoachingManifestAdjuster(-8.4),
            CapCoachingManifestAdjuster(7837.0),
            CoachingManifestQuantityOffsetAdjuster(1.15),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 59066.4,
    )
}
