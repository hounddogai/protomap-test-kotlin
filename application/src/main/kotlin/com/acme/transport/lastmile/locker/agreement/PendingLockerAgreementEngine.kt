package com.acme.transport.lastmile.locker.agreement

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending locker agreement calculation. */
interface PendingLockerAgreementModifier {
    fun describe(): String
    fun adjust(item: PendingLockerAgreement, value: Double): Double
}

data class PendingLockerAgreementEtaMinutesOffsetAdjuster(var weight: Double) : PendingLockerAgreementModifier {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: PendingLockerAgreement, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class CapPendingLockerAgreementAdjuster(var cap: Double) : PendingLockerAgreementModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingLockerAgreement, value: Double): Double = minOf(value, cap)
}

data class UnitPendingLockerAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingLockerAgreementModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: PendingLockerAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredPendingLockerAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingLockerAgreementModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingLockerAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingPendingLockerAgreementAdjuster(var step: Double) : PendingLockerAgreementModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingLockerAgreement, value: Double): Double = roundTo(value, step)
}

/** Runs the pending locker agreement steps in order and keeps a trace of each. */
data class PendingLockerAgreementEngine(
    var steps: MutableList<PendingLockerAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingLockerAgreement): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingLockerAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingLockerAgreementEngine(): PendingLockerAgreementEngine {
    return PendingLockerAgreementEngine(
        steps = mutableListOf<PendingLockerAgreementModifier>(
            PendingLockerAgreementEtaMinutesOffsetAdjuster(0.97),
            CapPendingLockerAgreementAdjuster(16697.0),
            UnitPendingLockerAgreementAdjuster(mutableMapOf<String, Double>("retail" to 1.07)),
            TieredPendingLockerAgreementAdjuster(
                mutableListOf<Double>(444.0, 8948.0),
                mutableListOf<Double>(0.95, 0.81),
            ),
            RoundingPendingLockerAgreementAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 9581.3,
    )
}
