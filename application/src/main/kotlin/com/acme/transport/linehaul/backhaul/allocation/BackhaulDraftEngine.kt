package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the backhaul draft calculation. */
interface BackhaulDraftAdjuster {
    fun describe(): String
    fun adjust(item: BackhaulDraft, value: Double): Double
}

data class BackhaulDraftDistanceKmOffsetAdjuster(var weight: Double) : BackhaulDraftAdjuster {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: BackhaulDraft, value: Double): Double = value + item.distanceKm * weight
}

data class RoundingBackhaulDraftAdjuster(var step: Double) : BackhaulDraftAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BackhaulDraft, value: Double): Double = roundTo(value, step)
}

data class LabelBackhaulDraftAdjuster(var factors: MutableMap<String, Double>) : BackhaulDraftAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: BackhaulDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentBackhaulDraftAdjuster(var percent: Double) : BackhaulDraftAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BackhaulDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedBackhaulDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BackhaulDraftAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BackhaulDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumBackhaulDraftAdjuster(var minimum: Double) : BackhaulDraftAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BackhaulDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the backhaul draft steps in order and keeps a trace of each. */
data class BackhaulDraftEngine(
    var steps: MutableList<BackhaulDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BackhaulDraft): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BackhaulDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBackhaulDraftEngine(flags: Registry): BackhaulDraftEngine {
    return BackhaulDraftEngine(
        steps = mutableListOf<BackhaulDraftAdjuster>(
            BackhaulDraftDistanceKmOffsetAdjuster(0.62),
            RoundingBackhaulDraftAdjuster(0.5),
            LabelBackhaulDraftAdjuster(mutableMapOf<String, Double>("west" to 0.79, "online" to 1.29)),
            PercentBackhaulDraftAdjuster(12.8),
            FlaggedBackhaulDraftAdjuster(flags, "backhaul-draft-adaptive", 5.9),
            MinimumBackhaulDraftAdjuster(39.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 27329.3,
    )
}
