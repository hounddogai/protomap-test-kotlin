package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient camera event ledger calculation. */
interface LenientCameraEventLedgerFactor {
    fun describe(): String
    fun adjust(item: LenientCameraEventLedger, value: Double): Double
}

data class MinimumLenientCameraEventLedgerAdjuster(var minimum: Double) : LenientCameraEventLedgerFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientCameraEventLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLenientCameraEventLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientCameraEventLedgerFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientCameraEventLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLenientCameraEventLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientCameraEventLedgerFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientCameraEventLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapLenientCameraEventLedgerAdjuster(var cap: Double) : LenientCameraEventLedgerFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientCameraEventLedger, value: Double): Double = minOf(value, cap)
}

data class CountryLenientCameraEventLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientCameraEventLedgerFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: LenientCameraEventLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient camera event ledger steps in order and keeps a trace of each. */
data class LenientCameraEventLedgerEngine(
    var steps: MutableList<LenientCameraEventLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientCameraEventLedger): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientCameraEventLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientCameraEventLedgerEngine(flags: Registry): LenientCameraEventLedgerEngine {
    return LenientCameraEventLedgerEngine(
        steps = mutableListOf<LenientCameraEventLedgerFactor>(
            MinimumLenientCameraEventLedgerAdjuster(31.4),
            FlaggedLenientCameraEventLedgerAdjuster(flags, "lenient-camera-event-ledger-scheduled", 2.3),
            TieredLenientCameraEventLedgerAdjuster(
                mutableListOf<Double>(5045.0, 8275.0),
                mutableListOf<Double>(0.97, 1.13),
            ),
            CapLenientCameraEventLedgerAdjuster(38298.0),
            CountryLenientCameraEventLedgerAdjuster(mutableMapOf<String, Double>("retail" to 1.18)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 40059.8,
    )
}
