package com.acme.transport.fleetops.defect.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual defect ledger calculation. */
interface ManualDefectLedgerStep {
    fun describe(): String
    fun adjust(item: ManualDefectLedger, value: Double): Double
}

data class RegionManualDefectLedgerAdjuster(var factors: MutableMap<String, Double>) : ManualDefectLedgerStep {
    override fun describe(): String = "by region"

    override fun adjust(item: ManualDefectLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentManualDefectLedgerAdjuster(var percent: Double) : ManualDefectLedgerStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualDefectLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedManualDefectLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualDefectLedgerStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualDefectLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapManualDefectLedgerAdjuster(var cap: Double) : ManualDefectLedgerStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualDefectLedger, value: Double): Double = minOf(value, cap)
}

data class TieredManualDefectLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualDefectLedgerStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualDefectLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual defect ledger steps in order and keeps a trace of each. */
data class ManualDefectLedgerEngine(
    var steps: MutableList<ManualDefectLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualDefectLedger): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualDefectLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualDefectLedgerEngine(flags: Registry): ManualDefectLedgerEngine {
    return ManualDefectLedgerEngine(
        steps = mutableListOf<ManualDefectLedgerStep>(
            RegionManualDefectLedgerAdjuster(mutableMapOf<String, Double>("central" to 1.27, "north" to 1.0)),
            PercentManualDefectLedgerAdjuster(11.0),
            FlaggedManualDefectLedgerAdjuster(flags, "manual-defect-ledger-seasonal", 5.9),
            CapManualDefectLedgerAdjuster(43612.0),
            TieredManualDefectLedgerAdjuster(
                mutableListOf<Double>(4680.0, 5270.0),
                mutableListOf<Double>(1.08, 0.91),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 9802.5,
    )
}
