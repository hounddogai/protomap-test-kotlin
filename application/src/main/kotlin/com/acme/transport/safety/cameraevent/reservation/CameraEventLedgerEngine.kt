package com.acme.transport.safety.cameraevent.reservation

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the camera event ledger calculation. */
interface CameraEventLedgerModifier {
    fun describe(): String
    fun adjust(item: CameraEventLedger, value: Double): Double
}

data class CapCameraEventLedgerAdjuster(var cap: Double) : CameraEventLedgerModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CameraEventLedger, value: Double): Double = minOf(value, cap)
}

data class PercentCameraEventLedgerAdjuster(var percent: Double) : CameraEventLedgerModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CameraEventLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleCameraEventLedgerAdjuster(var factors: MutableMap<String, Double>) : CameraEventLedgerModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: CameraEventLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the camera event ledger steps in order and keeps a trace of each. */
data class CameraEventLedgerEngine(
    var steps: MutableList<CameraEventLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CameraEventLedger): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CameraEventLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCameraEventLedgerEngine(): CameraEventLedgerEngine {
    return CameraEventLedgerEngine(
        steps = mutableListOf<CameraEventLedgerModifier>(
            CapCameraEventLedgerAdjuster(39319.0),
            PercentCameraEventLedgerAdjuster(10.4),
            TitleCameraEventLedgerAdjuster(mutableMapOf<String, Double>("west" to 1.39)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 82765.4,
    )
}
