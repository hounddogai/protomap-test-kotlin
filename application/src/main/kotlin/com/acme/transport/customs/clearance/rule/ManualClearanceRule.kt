package com.acme.transport.customs.clearance.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual clearance rule records of customs's clearance work, and the rules, stores, and calculations
// around them.

/** Where a manual clearance rule is in its life cycle. */
enum class ManualClearanceRuleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One manual clearance rule, with what the planners need to act on it. */
data class ManualClearanceRule(
    var code: String,
    var label: String,
    var archived: Boolean,
    var sequence: Int,
    var loadKg: Double,
    var status: ManualClearanceRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualClearanceRuleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualClearanceRuleStatus): Boolean {
        when (status) {
            ManualClearanceRuleStatus.PROPOSED -> {
                when (next) {
                    ManualClearanceRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualClearanceRuleStatus.APPROVED -> {
                when (next) {
                    ManualClearanceRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualClearanceRuleStatus.REJECTED -> return next == ManualClearanceRuleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ManualClearanceRuleStatus) {
        if (!canMoveTo(next)) {
            error("manual clearance rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual clearance rule"} $code"

    fun effectiveTotal(): Double = round((loadKg - sequence.toDouble()) * 100) / 100

    fun snapshot(): ManualClearanceRule {
        return ManualClearanceRule(
            code = code,
            label = label,
            archived = archived,
            sequence = sequence,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual clearance rule in status proposed. */
fun newManualClearanceRule(code: String, label: String): ManualClearanceRule {
    return ManualClearanceRule(
        code = code,
        label = label,
        archived = false,
        sequence = 0,
        loadKg = 0.0,
        status = ManualClearanceRuleStatus.PROPOSED,
    )
}
