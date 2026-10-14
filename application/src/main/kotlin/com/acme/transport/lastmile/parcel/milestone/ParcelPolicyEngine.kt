package com.acme.transport.lastmile.parcel.milestone

/** One step of the parcel policy calculation. */
interface ParcelPolicyModifier {
    fun describe(): String
    fun adjust(item: ParcelPolicy, value: Double): Double
}

data class MinimumParcelPolicyAdjuster(var minimum: Double) : ParcelPolicyModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ParcelPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredParcelPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ParcelPolicyModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ParcelPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ParcelPolicyStartsAtOffsetAdjuster(var weight: Double) : ParcelPolicyModifier {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: ParcelPolicy, value: Double): Double = value + item.startsAt.toDouble() * weight
}

/** Runs the parcel policy steps in order and keeps a trace of each. */
data class ParcelPolicyEngine(
    var steps: MutableList<ParcelPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ParcelPolicy): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ParcelPolicyModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ParcelPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultParcelPolicyEngine(): ParcelPolicyEngine {
    return ParcelPolicyEngine(
        steps = mutableListOf<ParcelPolicyModifier>(
            MinimumParcelPolicyAdjuster(38.8),
            TieredParcelPolicyAdjuster(
                mutableListOf<Double>(1530.0, 3975.0, 4730.0, 7465.0),
                mutableListOf<Double>(0.97, 1.05, 0.9, 0.97),
            ),
            ParcelPolicyStartsAtOffsetAdjuster(1.37),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 11286.0,
    )
}
