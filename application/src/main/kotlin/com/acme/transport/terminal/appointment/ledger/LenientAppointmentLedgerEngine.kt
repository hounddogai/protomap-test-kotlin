package com.acme.transport.terminal.appointment.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient appointment ledger calculation. */
interface LenientAppointmentLedgerModifier {
    fun describe(): String
    fun adjust(item: LenientAppointmentLedger, value: Double): Double
}

data class LenientAppointmentLedgerFloorValueOffsetAdjuster(
    var weight: Double,
) : LenientAppointmentLedgerModifier {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: LenientAppointmentLedger, value: Double): Double = value + item.floorValue * weight
}

data class NameLenientAppointmentLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientAppointmentLedgerModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: LenientAppointmentLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLenientAppointmentLedgerAdjuster(var percent: Double) : LenientAppointmentLedgerModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientAppointmentLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLenientAppointmentLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientAppointmentLedgerModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientAppointmentLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLenientAppointmentLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientAppointmentLedgerModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientAppointmentLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lenient appointment ledger steps in order and keeps a trace of each. */
data class LenientAppointmentLedgerEngine(
    var steps: MutableList<LenientAppointmentLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientAppointmentLedger): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientAppointmentLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientAppointmentLedgerEngine(flags: Registry): LenientAppointmentLedgerEngine {
    return LenientAppointmentLedgerEngine(
        steps = mutableListOf<LenientAppointmentLedgerModifier>(
            LenientAppointmentLedgerFloorValueOffsetAdjuster(1.93),
            NameLenientAppointmentLedgerAdjuster(
                mutableMapOf<String, Double>("central" to 1.04, "east" to 0.91),
            ),
            PercentLenientAppointmentLedgerAdjuster(-3.8),
            FlaggedLenientAppointmentLedgerAdjuster(flags, "lenient-appointment-ledger-strict", -3.8),
            TieredLenientAppointmentLedgerAdjuster(
                mutableListOf<Double>(2220.0, 5718.0, 8600.0, 9218.0),
                mutableListOf<Double>(0.86, 1.06, 1.18, 1.16),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 14429.5,
    )
}
