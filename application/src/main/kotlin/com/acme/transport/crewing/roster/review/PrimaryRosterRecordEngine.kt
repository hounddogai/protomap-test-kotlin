package com.acme.transport.crewing.roster.review

/** One step of the primary roster record calculation. */
interface PrimaryRosterRecordStep {
    fun describe(): String
    fun adjust(item: PrimaryRosterRecord, value: Double): Double
}

data class DepotPrimaryRosterRecordAdjuster(var factors: MutableMap<String, Double>) : PrimaryRosterRecordStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: PrimaryRosterRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumPrimaryRosterRecordAdjuster(var minimum: Double) : PrimaryRosterRecordStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryRosterRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPrimaryRosterRecordAdjuster(var cap: Double) : PrimaryRosterRecordStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryRosterRecord, value: Double): Double = minOf(value, cap)
}

data class PercentPrimaryRosterRecordAdjuster(var percent: Double) : PrimaryRosterRecordStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryRosterRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the primary roster record steps in order and keeps a trace of each. */
data class PrimaryRosterRecordEngine(
    var steps: MutableList<PrimaryRosterRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryRosterRecord): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryRosterRecordStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryRosterRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryRosterRecordEngine(): PrimaryRosterRecordEngine {
    return PrimaryRosterRecordEngine(
        steps = mutableListOf<PrimaryRosterRecordStep>(
            DepotPrimaryRosterRecordAdjuster(mutableMapOf<String, Double>("west" to 1.15)),
            MinimumPrimaryRosterRecordAdjuster(43.4),
            CapPrimaryRosterRecordAdjuster(1024.0),
            PercentPrimaryRosterRecordAdjuster(-2.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 99496.9,
    )
}
