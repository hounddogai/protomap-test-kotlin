package com.acme.transport.fleetops.defect.profile

/** One step of the manual defect milestone calculation. */
interface ManualDefectMilestoneFactor {
    fun describe(): String
    fun adjust(item: ManualDefectMilestone, value: Double): Double
}

data class PercentManualDefectMilestoneAdjuster(var percent: Double) : ManualDefectMilestoneFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualDefectMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ManualDefectMilestoneCeilingOffsetAdjuster(var weight: Double) : ManualDefectMilestoneFactor {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: ManualDefectMilestone, value: Double): Double = value + item.ceiling * weight
}

data class MinimumManualDefectMilestoneAdjuster(var minimum: Double) : ManualDefectMilestoneFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualDefectMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredManualDefectMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualDefectMilestoneFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualDefectMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CountryManualDefectMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualDefectMilestoneFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: ManualDefectMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual defect milestone steps in order and keeps a trace of each. */
data class ManualDefectMilestoneEngine(
    var steps: MutableList<ManualDefectMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualDefectMilestone): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualDefectMilestoneFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ManualDefectMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualDefectMilestoneEngine(): ManualDefectMilestoneEngine {
    return ManualDefectMilestoneEngine(
        steps = mutableListOf<ManualDefectMilestoneFactor>(
            PercentManualDefectMilestoneAdjuster(-4.5),
            ManualDefectMilestoneCeilingOffsetAdjuster(1.26),
            MinimumManualDefectMilestoneAdjuster(2.3),
            TieredManualDefectMilestoneAdjuster(
                mutableListOf<Double>(7349.0, 8815.0, 9699.0),
                mutableListOf<Double>(1.14, 1.12, 1.2),
            ),
            CountryManualDefectMilestoneAdjuster(mutableMapOf<String, Double>("central" to 1.23)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 7140.5,
    )
}
