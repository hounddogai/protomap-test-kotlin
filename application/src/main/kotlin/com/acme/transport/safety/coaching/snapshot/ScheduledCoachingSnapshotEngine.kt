package com.acme.transport.safety.coaching.snapshot

/** One step of the scheduled coaching snapshot calculation. */
interface ScheduledCoachingSnapshotModifier {
    fun describe(): String
    fun adjust(item: ScheduledCoachingSnapshot, value: Double): Double
}

data class ScheduledCoachingSnapshotEtaMinutesOffsetAdjuster(
    var weight: Double,
) : ScheduledCoachingSnapshotModifier {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: ScheduledCoachingSnapshot, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class TieredScheduledCoachingSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledCoachingSnapshotModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledCoachingSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RegionScheduledCoachingSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledCoachingSnapshotModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: ScheduledCoachingSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapScheduledCoachingSnapshotAdjuster(var cap: Double) : ScheduledCoachingSnapshotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledCoachingSnapshot, value: Double): Double = minOf(value, cap)
}

data class MinimumScheduledCoachingSnapshotAdjuster(var minimum: Double) : ScheduledCoachingSnapshotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledCoachingSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scheduled coaching snapshot steps in order and keeps a trace of each. */
data class ScheduledCoachingSnapshotEngine(
    var steps: MutableList<ScheduledCoachingSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledCoachingSnapshot): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledCoachingSnapshotModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledCoachingSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledCoachingSnapshotEngine(): ScheduledCoachingSnapshotEngine {
    return ScheduledCoachingSnapshotEngine(
        steps = mutableListOf<ScheduledCoachingSnapshotModifier>(
            ScheduledCoachingSnapshotEtaMinutesOffsetAdjuster(1.18),
            TieredScheduledCoachingSnapshotAdjuster(
                mutableListOf<Double>(897.0, 2988.0, 9400.0),
                mutableListOf<Double>(1.01, 0.99, 0.85),
            ),
            RegionScheduledCoachingSnapshotAdjuster(
                mutableMapOf<String, Double>("online" to 0.94, "central" to 0.99),
            ),
            CapScheduledCoachingSnapshotAdjuster(10540.0),
            MinimumScheduledCoachingSnapshotAdjuster(27.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 53951.0,
    )
}
