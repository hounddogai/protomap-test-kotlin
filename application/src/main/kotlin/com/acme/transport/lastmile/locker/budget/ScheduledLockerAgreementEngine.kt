package com.acme.transport.lastmile.locker.budget

import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled locker agreement calculation. */
interface ScheduledLockerAgreementModifier {
    fun describe(): String
    fun adjust(item: ScheduledLockerAgreement, value: Double): Double
}

data class CountryScheduledLockerAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledLockerAgreementModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: ScheduledLockerAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ScheduledLockerAgreementEtaMinutesOffsetAdjuster(
    var weight: Double,
) : ScheduledLockerAgreementModifier {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: ScheduledLockerAgreement, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class RoundingScheduledLockerAgreementAdjuster(var step: Double) : ScheduledLockerAgreementModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledLockerAgreement, value: Double): Double = roundTo(value, step)
}

/** Runs the scheduled locker agreement steps in order and keeps a trace of each. */
data class ScheduledLockerAgreementEngine(
    var steps: MutableList<ScheduledLockerAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledLockerAgreement): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledLockerAgreementModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledLockerAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledLockerAgreementEngine(): ScheduledLockerAgreementEngine {
    return ScheduledLockerAgreementEngine(
        steps = mutableListOf<ScheduledLockerAgreementModifier>(
            CountryScheduledLockerAgreementAdjuster(mutableMapOf<String, Double>("online" to 1.4)),
            ScheduledLockerAgreementEtaMinutesOffsetAdjuster(1.16),
            RoundingScheduledLockerAgreementAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 53185.3,
    )
}
