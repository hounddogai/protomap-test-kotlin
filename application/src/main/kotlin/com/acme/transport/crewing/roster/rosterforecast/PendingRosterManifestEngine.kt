package com.acme.transport.crewing.roster.rosterforecast

/** One step of the pending roster manifest calculation. */
interface PendingRosterManifestStep {
    fun describe(): String
    fun adjust(item: PendingRosterManifest, value: Double): Double
}

data class CapPendingRosterManifestAdjuster(var cap: Double) : PendingRosterManifestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingRosterManifest, value: Double): Double = minOf(value, cap)
}

data class PendingRosterManifestSequenceOffsetAdjuster(var weight: Double) : PendingRosterManifestStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: PendingRosterManifest, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class LabelPendingRosterManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingRosterManifestStep {
    override fun describe(): String = "by label"

    override fun adjust(item: PendingRosterManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending roster manifest steps in order and keeps a trace of each. */
data class PendingRosterManifestEngine(
    var steps: MutableList<PendingRosterManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingRosterManifest): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingRosterManifestStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingRosterManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingRosterManifestEngine(): PendingRosterManifestEngine {
    return PendingRosterManifestEngine(
        steps = mutableListOf<PendingRosterManifestStep>(
            CapPendingRosterManifestAdjuster(14898.0),
            PendingRosterManifestSequenceOffsetAdjuster(0.03),
            LabelPendingRosterManifestAdjuster(
                mutableMapOf<String, Double>("north" to 0.81, "retail" to 0.96, "west" to 1.32),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 33622.3,
    )
}
