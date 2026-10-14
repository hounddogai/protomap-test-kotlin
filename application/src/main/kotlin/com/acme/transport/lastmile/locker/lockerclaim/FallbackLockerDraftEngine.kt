package com.acme.transport.lastmile.locker.lockerclaim

import com.acme.transport.kit.flags.Registry

/** One step of the fallback locker draft calculation. */
interface FallbackLockerDraftAdjuster {
    fun describe(): String
    fun adjust(item: FallbackLockerDraft, value: Double): Double
}

data class MinimumFallbackLockerDraftAdjuster(var minimum: Double) : FallbackLockerDraftAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackLockerDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedFallbackLockerDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackLockerDraftAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackLockerDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionFallbackLockerDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackLockerDraftAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: FallbackLockerDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FallbackLockerDraftAttemptsOffsetAdjuster(var weight: Double) : FallbackLockerDraftAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: FallbackLockerDraft, value: Double): Double = value + item.attempts.toDouble() * weight
}

/** Runs the fallback locker draft steps in order and keeps a trace of each. */
data class FallbackLockerDraftEngine(
    var steps: MutableList<FallbackLockerDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackLockerDraft): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackLockerDraftAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackLockerDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackLockerDraftEngine(flags: Registry): FallbackLockerDraftEngine {
    return FallbackLockerDraftEngine(
        steps = mutableListOf<FallbackLockerDraftAdjuster>(
            MinimumFallbackLockerDraftAdjuster(26.8),
            FlaggedFallbackLockerDraftAdjuster(flags, "fallback-locker-draft-tiered", -9.6),
            RegionFallbackLockerDraftAdjuster(mutableMapOf<String, Double>("retail" to 0.88)),
            FallbackLockerDraftAttemptsOffsetAdjuster(0.61),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 92524.6,
    )
}
