package com.acme.transport.terminal.dockdoor.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending dock door agreement calculation. */
interface PendingDockDoorAgreementAdjuster {
    fun describe(): String
    fun adjust(item: PendingDockDoorAgreement, value: Double): Double
}

data class RoundingPendingDockDoorAgreementAdjuster(var step: Double) : PendingDockDoorAgreementAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingDockDoorAgreement, value: Double): Double = roundTo(value, step)
}

data class MinimumPendingDockDoorAgreementAdjuster(var minimum: Double) : PendingDockDoorAgreementAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingDockDoorAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CountryPendingDockDoorAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingDockDoorAgreementAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: PendingDockDoorAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPendingDockDoorAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingDockDoorAgreementAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingDockDoorAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPendingDockDoorAgreementAdjuster(var cap: Double) : PendingDockDoorAgreementAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingDockDoorAgreement, value: Double): Double = minOf(value, cap)
}

data class TieredPendingDockDoorAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingDockDoorAgreementAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingDockDoorAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the pending dock door agreement steps in order and keeps a trace of each. */
data class PendingDockDoorAgreementEngine(
    var steps: MutableList<PendingDockDoorAgreementAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingDockDoorAgreement): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingDockDoorAgreementAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingDockDoorAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingDockDoorAgreementEngine(flags: Registry): PendingDockDoorAgreementEngine {
    return PendingDockDoorAgreementEngine(
        steps = mutableListOf<PendingDockDoorAgreementAdjuster>(
            RoundingPendingDockDoorAgreementAdjuster(0.05),
            MinimumPendingDockDoorAgreementAdjuster(49.5),
            CountryPendingDockDoorAgreementAdjuster(
                mutableMapOf<String, Double>("west" to 1.06, "retail" to 1.06),
            ),
            FlaggedPendingDockDoorAgreementAdjuster(flags, "pending-dock-door-agreement-default", -0.5),
            CapPendingDockDoorAgreementAdjuster(7781.0),
            TieredPendingDockDoorAgreementAdjuster(
                mutableListOf<Double>(4878.0, 7065.0, 8546.0),
                mutableListOf<Double>(0.86, 0.95, 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 51961.5,
    )
}
