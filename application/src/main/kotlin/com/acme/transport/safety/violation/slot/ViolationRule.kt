package com.acme.transport.safety.violation.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation rule is in its life cycle. */
enum class ViolationRuleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A violation rule. Its code is unique within a tenant. */
data class ViolationRule(
    var code: String,
    var label: String,
    var ownerId: String,
    var threshold: Double,
    var limit: Int,
    var status: ViolationRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationRuleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationRuleStatus): Boolean {
        when (status) {
            ViolationRuleStatus.CREATED -> {
                when (next) {
                    ViolationRuleStatus.VALIDATED, ViolationRuleStatus.RETIRED -> return true
                }
                return false
            }
            ViolationRuleStatus.VALIDATED -> return next == ViolationRuleStatus.PUBLISHED || next == ViolationRuleStatus.RETIRED
            ViolationRuleStatus.PUBLISHED -> {
                when (next) {
                    ViolationRuleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ViolationRuleStatus) {
        if (!canMoveTo(next)) {
            error("violation rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveTotal(): Double = round((threshold - limit.toDouble()) * 100) / 100

    fun snapshot(): ViolationRule {
        return ViolationRule(
            code = code,
            label = label,
            ownerId = ownerId,
            threshold = threshold,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a violation rule in status created. */
fun newViolationRule(code: String, label: String): ViolationRule {
    return ViolationRule(
        code = code,
        label = label,
        ownerId = "",
        threshold = 0.0,
        limit = 0,
        status = ViolationRuleStatus.CREATED,
    )
}
