package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the locker audit calculation. */
interface LockerAuditFactor {
    fun describe(): String
    fun adjust(item: LockerAudit, value: Double): Double
}

data class LockerAuditUpdatedAtOffsetAdjuster(var weight: Double) : LockerAuditFactor {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: LockerAudit, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

data class FlaggedLockerAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LockerAuditFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LockerAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLockerAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LockerAuditFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LockerAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the locker audit steps in order and keeps a trace of each. */
data class LockerAuditEngine(
    var steps: MutableList<LockerAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LockerAudit): Double {
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

    fun evaluateAll(items: MutableList<LockerAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLockerAuditEngine(flags: Registry): LockerAuditEngine {
    return LockerAuditEngine(
        steps = mutableListOf<LockerAuditFactor>(
            LockerAuditUpdatedAtOffsetAdjuster(1.7),
            FlaggedLockerAuditAdjuster(flags, "locker-audit-lenient", -4.0),
            TieredLockerAuditAdjuster(
                mutableListOf<Double>(6677.0, 7136.0, 8163.0),
                mutableListOf<Double>(1.13, 1.13, 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 53863.6,
    )
}
