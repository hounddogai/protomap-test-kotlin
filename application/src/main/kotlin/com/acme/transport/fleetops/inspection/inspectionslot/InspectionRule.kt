package com.acme.transport.fleetops.inspection.inspectionslot

import com.acme.transport.kit.text.normalize

/** Where a inspection rule is in its life cycle. */
enum class InspectionRuleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A inspection rule. Its code is unique within a tenant. */
data class InspectionRule(
    var code: String,
    var label: String,
    var trailerId: String,
    var externalRef: String,
    var summary: String,
    var score: Double,
    var status: InspectionRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionRuleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionRuleStatus): Boolean {
        when (status) {
            InspectionRuleStatus.PROPOSED -> {
                when (next) {
                    InspectionRuleStatus.APPROVED, InspectionRuleStatus.REJECTED -> return true
                }
                return false
            }
            InspectionRuleStatus.APPROVED -> {
                when (next) {
                    InspectionRuleStatus.REJECTED, InspectionRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
            InspectionRuleStatus.REJECTED -> return next == InspectionRuleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: InspectionRuleStatus) {
        if (!canMoveTo(next)) {
            error("inspection rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"inspection rule"} $code"

    fun measuredValue(): Double = score

    fun snapshot(): InspectionRule {
        return InspectionRule(
            code = code,
            label = label,
            trailerId = trailerId,
            externalRef = externalRef,
            summary = summary,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a inspection rule in status proposed. */
fun newInspectionRule(code: String, label: String): InspectionRule {
    return InspectionRule(
        code = code,
        label = label,
        trailerId = "",
        externalRef = "",
        summary = "",
        score = 0.0,
        status = InspectionRuleStatus.PROPOSED,
    )
}
