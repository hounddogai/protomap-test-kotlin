package com.acme.transport.linehaul.trailer.claim

/** One step of the default trailer claim calculation. */
interface DefaultTrailerClaimAdjuster {
    fun describe(): String
    fun adjust(item: DefaultTrailerClaim, value: Double): Double
}

data class DefaultTrailerClaimFloorValueOffsetAdjuster(var weight: Double) : DefaultTrailerClaimAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: DefaultTrailerClaim, value: Double): Double = value + item.floorValue * weight
}

data class DepotDefaultTrailerClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultTrailerClaimAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: DefaultTrailerClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDefaultTrailerClaimAdjuster(var percent: Double) : DefaultTrailerClaimAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultTrailerClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumDefaultTrailerClaimAdjuster(var minimum: Double) : DefaultTrailerClaimAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultTrailerClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDefaultTrailerClaimAdjuster(var cap: Double) : DefaultTrailerClaimAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultTrailerClaim, value: Double): Double = minOf(value, cap)
}

data class TieredDefaultTrailerClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultTrailerClaimAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultTrailerClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the default trailer claim steps in order and keeps a trace of each. */
data class DefaultTrailerClaimEngine(
    var steps: MutableList<DefaultTrailerClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultTrailerClaim): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultTrailerClaimAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultTrailerClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultTrailerClaimEngine(): DefaultTrailerClaimEngine {
    return DefaultTrailerClaimEngine(
        steps = mutableListOf<DefaultTrailerClaimAdjuster>(
            DefaultTrailerClaimFloorValueOffsetAdjuster(1.99),
            DepotDefaultTrailerClaimAdjuster(
                mutableMapOf<String, Double>("central" to 0.89, "retail" to 1.19, "west" to 1.01),
            ),
            PercentDefaultTrailerClaimAdjuster(-16.3),
            MinimumDefaultTrailerClaimAdjuster(33.9),
            CapDefaultTrailerClaimAdjuster(20997.0),
            TieredDefaultTrailerClaimAdjuster(
                mutableListOf<Double>(96.0, 5250.0, 5635.0),
                mutableListOf<Double>(0.84, 1.16, 0.86),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 40045.5,
    )
}
