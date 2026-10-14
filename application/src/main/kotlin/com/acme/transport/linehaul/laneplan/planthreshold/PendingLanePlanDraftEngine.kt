package com.acme.transport.linehaul.laneplan.planthreshold

/** One step of the pending lane plan draft calculation. */
interface PendingLanePlanDraftStep {
    fun describe(): String
    fun adjust(item: PendingLanePlanDraft, value: Double): Double
}

data class MinimumPendingLanePlanDraftAdjuster(var minimum: Double) : PendingLanePlanDraftStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingLanePlanDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PendingLanePlanDraftBatchSizeOffsetAdjuster(var weight: Double) : PendingLanePlanDraftStep {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: PendingLanePlanDraft, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class NamePendingLanePlanDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingLanePlanDraftStep {
    override fun describe(): String = "by name"

    override fun adjust(item: PendingLanePlanDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPendingLanePlanDraftAdjuster(var percent: Double) : PendingLanePlanDraftStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingLanePlanDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the pending lane plan draft steps in order and keeps a trace of each. */
data class PendingLanePlanDraftEngine(
    var steps: MutableList<PendingLanePlanDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingLanePlanDraft): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingLanePlanDraftStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingLanePlanDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingLanePlanDraftEngine(): PendingLanePlanDraftEngine {
    return PendingLanePlanDraftEngine(
        steps = mutableListOf<PendingLanePlanDraftStep>(
            MinimumPendingLanePlanDraftAdjuster(30.1),
            PendingLanePlanDraftBatchSizeOffsetAdjuster(1.39),
            NamePendingLanePlanDraftAdjuster(
                mutableMapOf<String, Double>("east" to 1.3, "central" to 0.94, "north" to 1.4),
            ),
            PercentPendingLanePlanDraftAdjuster(-7.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 55209.9,
    )
}
