package com.acme.transport.safety.incident.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident rule is in its life cycle. */
enum class IncidentRuleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A incident rule. Its code is unique within a tenant. */
data class IncidentRule(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var sequence: Int,
    var segment: String,
    var windowDays: Int,
    var routeCode: String,
    var carrier: String,
    var reviewer: String,
    var margin: Double,
    var maximum: Double,
    var status: IncidentRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentRuleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentRuleStatus): Boolean {
        when (status) {
            IncidentRuleStatus.DRAFT -> {
                when (next) {
                    IncidentRuleStatus.PENDING, IncidentRuleStatus.SUSPENDED -> return true
                }
                return false
            }
            IncidentRuleStatus.PENDING -> {
                when (next) {
                    IncidentRuleStatus.CLOSED -> return true
                }
                return false
            }
            IncidentRuleStatus.ACTIVE -> {
                when (next) {
                    IncidentRuleStatus.SUSPENDED, IncidentRuleStatus.CLOSED -> return true
                }
                return false
            }
            IncidentRuleStatus.SUSPENDED -> {
                when (next) {
                    IncidentRuleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentRuleStatus) {
        if (!canMoveTo(next)) {
            error("incident rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netTotal(): Double = round(maximum * sequence.toDouble() * 100) / 100

    fun snapshot(): IncidentRule {
        return IncidentRule(
            code = code,
            label = label,
            attributes = attributes,
            sequence = sequence,
            segment = segment,
            windowDays = windowDays,
            routeCode = routeCode,
            carrier = carrier,
            reviewer = reviewer,
            margin = margin,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a incident rule in status draft. */
fun newIncidentRule(code: String, label: String): IncidentRule {
    return IncidentRule(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        sequence = 0,
        segment = "",
        windowDays = 0,
        routeCode = "",
        carrier = "",
        reviewer = "",
        margin = 0.0,
        maximum = 0.0,
        status = IncidentRuleStatus.DRAFT,
    )
}
