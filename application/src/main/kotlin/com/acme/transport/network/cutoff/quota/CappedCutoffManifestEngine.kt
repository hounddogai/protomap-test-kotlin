package com.acme.transport.network.cutoff.quota

/** One step of the capped cutoff manifest calculation. */
interface CappedCutoffManifestStep {
    fun describe(): String
    fun adjust(item: CappedCutoffManifest, value: Double): Double
}

data class CapCappedCutoffManifestAdjuster(var cap: Double) : CappedCutoffManifestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedCutoffManifest, value: Double): Double = minOf(value, cap)
}

data class PercentCappedCutoffManifestAdjuster(var percent: Double) : CappedCutoffManifestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedCutoffManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredCappedCutoffManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedCutoffManifestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedCutoffManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCappedCutoffManifestAdjuster(var minimum: Double) : CappedCutoffManifestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedCutoffManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the capped cutoff manifest steps in order and keeps a trace of each. */
data class CappedCutoffManifestEngine(
    var steps: MutableList<CappedCutoffManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedCutoffManifest): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedCutoffManifestStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedCutoffManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedCutoffManifestEngine(): CappedCutoffManifestEngine {
    return CappedCutoffManifestEngine(
        steps = mutableListOf<CappedCutoffManifestStep>(
            CapCappedCutoffManifestAdjuster(27493.0),
            PercentCappedCutoffManifestAdjuster(2.4),
            TieredCappedCutoffManifestAdjuster(
                mutableListOf<Double>(11.0, 385.0, 3365.0, 5957.0),
                mutableListOf<Double>(1.17, 1.02, 1.0, 0.99),
            ),
            MinimumCappedCutoffManifestAdjuster(24.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 94182.6,
    )
}
