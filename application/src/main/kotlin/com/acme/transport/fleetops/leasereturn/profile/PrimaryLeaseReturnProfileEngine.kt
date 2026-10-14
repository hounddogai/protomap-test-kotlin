package com.acme.transport.fleetops.leasereturn.profile

/** One step of the primary lease return profile calculation. */
interface PrimaryLeaseReturnProfileStep {
    fun describe(): String
    fun adjust(item: PrimaryLeaseReturnProfile, value: Double): Double
}

data class NamePrimaryLeaseReturnProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryLeaseReturnProfileStep {
    override fun describe(): String = "by name"

    override fun adjust(item: PrimaryLeaseReturnProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PrimaryLeaseReturnProfileScoreOffsetAdjuster(var weight: Double) : PrimaryLeaseReturnProfileStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: PrimaryLeaseReturnProfile, value: Double): Double = value + item.score * weight
}

data class MinimumPrimaryLeaseReturnProfileAdjuster(var minimum: Double) : PrimaryLeaseReturnProfileStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryLeaseReturnProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the primary lease return profile steps in order and keeps a trace of each. */
data class PrimaryLeaseReturnProfileEngine(
    var steps: MutableList<PrimaryLeaseReturnProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryLeaseReturnProfile): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryLeaseReturnProfileStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryLeaseReturnProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryLeaseReturnProfileEngine(): PrimaryLeaseReturnProfileEngine {
    return PrimaryLeaseReturnProfileEngine(
        steps = mutableListOf<PrimaryLeaseReturnProfileStep>(
            NamePrimaryLeaseReturnProfileAdjuster(
                mutableMapOf<String, Double>("south" to 1.01, "online" to 0.97),
            ),
            PrimaryLeaseReturnProfileScoreOffsetAdjuster(1.66),
            MinimumPrimaryLeaseReturnProfileAdjuster(48.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 46188.3,
    )
}
