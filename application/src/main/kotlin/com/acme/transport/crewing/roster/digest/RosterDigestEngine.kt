package com.acme.transport.crewing.roster.digest

/** One step of the roster digest calculation. */
interface RosterDigestModifier {
    fun describe(): String
    fun adjust(item: RosterDigest, value: Double): Double
}

data class MinimumRosterDigestAdjuster(var minimum: Double) : RosterDigestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RosterDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRosterDigestAdjuster(var cap: Double) : RosterDigestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RosterDigest, value: Double): Double = minOf(value, cap)
}

data class RosterDigestDistanceKmOffsetAdjuster(var weight: Double) : RosterDigestModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: RosterDigest, value: Double): Double = value + item.distanceKm * weight
}

data class TieredRosterDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterDigestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameRosterDigestAdjuster(var factors: MutableMap<String, Double>) : RosterDigestModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: RosterDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the roster digest steps in order and keeps a trace of each. */
data class RosterDigestEngine(
    var steps: MutableList<RosterDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RosterDigest): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RosterDigestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RosterDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRosterDigestEngine(): RosterDigestEngine {
    return RosterDigestEngine(
        steps = mutableListOf<RosterDigestModifier>(
            MinimumRosterDigestAdjuster(42.1),
            CapRosterDigestAdjuster(30749.0),
            RosterDigestDistanceKmOffsetAdjuster(0.26),
            TieredRosterDigestAdjuster(
                mutableListOf<Double>(1420.0, 2926.0, 7131.0),
                mutableListOf<Double>(0.94, 0.91, 1.15),
            ),
            NameRosterDigestAdjuster(mutableMapOf<String, Double>("west" to 0.86, "retail" to 1.24)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 24995.5,
    )
}
