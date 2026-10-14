package com.acme.transport.customs.declaration.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the declaration audit calculation. */
interface DeclarationAuditFactor {
    fun describe(): String
    fun adjust(item: DeclarationAudit, value: Double): Double
}

data class CapDeclarationAuditAdjuster(var cap: Double) : DeclarationAuditFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DeclarationAudit, value: Double): Double = minOf(value, cap)
}

data class FlaggedDeclarationAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DeclarationAuditFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DeclarationAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DeclarationAuditCostOffsetAdjuster(var weight: Double) : DeclarationAuditFactor {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: DeclarationAudit, value: Double): Double = value + item.cost * weight
}

/** Runs the declaration audit steps in order and keeps a trace of each. */
data class DeclarationAuditEngine(
    var steps: MutableList<DeclarationAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DeclarationAudit): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DeclarationAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDeclarationAuditEngine(flags: Registry): DeclarationAuditEngine {
    return DeclarationAuditEngine(
        steps = mutableListOf<DeclarationAuditFactor>(
            CapDeclarationAuditAdjuster(9414.0),
            FlaggedDeclarationAuditAdjuster(flags, "declaration-audit-pending", -5.6),
            DeclarationAuditCostOffsetAdjuster(1.73),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 53210.5,
    )
}
