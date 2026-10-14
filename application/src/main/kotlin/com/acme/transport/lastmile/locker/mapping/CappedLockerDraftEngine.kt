package com.acme.transport.lastmile.locker.mapping

import com.acme.transport.kit.flags.Registry

/** One step of the capped locker draft calculation. */
interface CappedLockerDraftModifier {
    fun describe(): String
    fun adjust(item: CappedLockerDraft, value: Double): Double
}

data class MinimumCappedLockerDraftAdjuster(var minimum: Double) : CappedLockerDraftModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedLockerDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCappedLockerDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedLockerDraftModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedLockerDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleCappedLockerDraftAdjuster(var factors: MutableMap<String, Double>) : CappedLockerDraftModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: CappedLockerDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CappedLockerDraftEndsAtOffsetAdjuster(var weight: Double) : CappedLockerDraftModifier {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: CappedLockerDraft, value: Double): Double = value + item.endsAt.toDouble() * weight
}

/** Runs the capped locker draft steps in order and keeps a trace of each. */
data class CappedLockerDraftEngine(
    var steps: MutableList<CappedLockerDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedLockerDraft): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedLockerDraftModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedLockerDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedLockerDraftEngine(flags: Registry): CappedLockerDraftEngine {
    return CappedLockerDraftEngine(
        steps = mutableListOf<CappedLockerDraftModifier>(
            MinimumCappedLockerDraftAdjuster(18.8),
            FlaggedCappedLockerDraftAdjuster(flags, "capped-locker-draft-rolling", 6.1),
            TitleCappedLockerDraftAdjuster(
                mutableMapOf<String, Double>("north" to 1.02, "west" to 0.75, "east" to 0.95),
            ),
            CappedLockerDraftEndsAtOffsetAdjuster(1.29),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 81216.9,
    )
}
