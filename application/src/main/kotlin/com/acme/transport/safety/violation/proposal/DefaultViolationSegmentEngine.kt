package com.acme.transport.safety.violation.proposal

/** One step of the default violation segment calculation. */
interface DefaultViolationSegmentModifier {
    fun describe(): String
    fun adjust(item: DefaultViolationSegment, value: Double): Double
}

data class NameDefaultViolationSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultViolationSegmentModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: DefaultViolationSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDefaultViolationSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultViolationSegmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultViolationSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDefaultViolationSegmentAdjuster(var minimum: Double) : DefaultViolationSegmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultViolationSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDefaultViolationSegmentAdjuster(var percent: Double) : DefaultViolationSegmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultViolationSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DefaultViolationSegmentCapacityOffsetAdjuster(var weight: Double) : DefaultViolationSegmentModifier {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: DefaultViolationSegment, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

/** Runs the default violation segment steps in order and keeps a trace of each. */
data class DefaultViolationSegmentEngine(
    var steps: MutableList<DefaultViolationSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultViolationSegment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultViolationSegmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultViolationSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultViolationSegmentEngine(): DefaultViolationSegmentEngine {
    return DefaultViolationSegmentEngine(
        steps = mutableListOf<DefaultViolationSegmentModifier>(
            NameDefaultViolationSegmentAdjuster(mutableMapOf<String, Double>("south" to 0.99, "east" to 1.16)),
            TieredDefaultViolationSegmentAdjuster(
                mutableListOf<Double>(4778.0, 9932.0),
                mutableListOf<Double>(1.16, 1.1),
            ),
            MinimumDefaultViolationSegmentAdjuster(16.9),
            PercentDefaultViolationSegmentAdjuster(-10.3),
            DefaultViolationSegmentCapacityOffsetAdjuster(1.47),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 91305.0,
    )
}
