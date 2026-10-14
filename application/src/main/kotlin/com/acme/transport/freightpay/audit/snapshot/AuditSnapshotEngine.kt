package com.acme.transport.freightpay.audit.snapshot

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the audit snapshot calculation. */
interface AuditSnapshotFactor {
    fun describe(): String
    fun adjust(item: AuditSnapshot, value: Double): Double
}

data class PercentAuditSnapshotAdjuster(var percent: Double) : AuditSnapshotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AuditSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapAuditSnapshotAdjuster(var cap: Double) : AuditSnapshotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AuditSnapshot, value: Double): Double = minOf(value, cap)
}

data class AuditSnapshotUpdatedAtOffsetAdjuster(var weight: Double) : AuditSnapshotFactor {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: AuditSnapshot, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

data class TieredAuditSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AuditSnapshotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AuditSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the audit snapshot steps in order and keeps a trace of each. */
data class AuditSnapshotEngine(
    var steps: MutableList<AuditSnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AuditSnapshot): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AuditSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAuditSnapshotEngine(): AuditSnapshotEngine {
    return AuditSnapshotEngine(
        steps = mutableListOf<AuditSnapshotFactor>(
            PercentAuditSnapshotAdjuster(-11.0),
            CapAuditSnapshotAdjuster(36982.0),
            AuditSnapshotUpdatedAtOffsetAdjuster(1.82),
            TieredAuditSnapshotAdjuster(
                mutableListOf<Double>(6574.0, 9927.0),
                mutableListOf<Double>(0.82, 1.16),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 13408.9,
    )
}
