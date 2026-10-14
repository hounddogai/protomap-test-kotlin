package com.acme.transport.linehaul.relay.group

import com.acme.transport.kit.flags.Registry

/** One step of the capped relay review calculation. */
interface CappedRelayReviewModifier {
    fun describe(): String
    fun adjust(item: CappedRelayReview, value: Double): Double
}

data class MinimumCappedRelayReviewAdjuster(var minimum: Double) : CappedRelayReviewModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedRelayReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCappedRelayReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedRelayReviewModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedRelayReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCappedRelayReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedRelayReviewModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedRelayReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCappedRelayReviewAdjuster(var percent: Double) : CappedRelayReviewModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedRelayReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LocaleCappedRelayReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedRelayReviewModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: CappedRelayReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped relay review steps in order and keeps a trace of each. */
data class CappedRelayReviewEngine(
    var steps: MutableList<CappedRelayReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedRelayReview): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedRelayReviewModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedRelayReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedRelayReviewEngine(flags: Registry): CappedRelayReviewEngine {
    return CappedRelayReviewEngine(
        steps = mutableListOf<CappedRelayReviewModifier>(
            MinimumCappedRelayReviewAdjuster(9.7),
            FlaggedCappedRelayReviewAdjuster(flags, "capped-relay-review-adaptive", 9.3),
            TieredCappedRelayReviewAdjuster(
                mutableListOf<Double>(198.0, 5634.0, 7839.0, 9536.0),
                mutableListOf<Double>(0.8, 1.02, 0.88, 1.06),
            ),
            PercentCappedRelayReviewAdjuster(21.9),
            LocaleCappedRelayReviewAdjuster(mutableMapOf<String, Double>("retail" to 0.83)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 24913.2,
    )
}
