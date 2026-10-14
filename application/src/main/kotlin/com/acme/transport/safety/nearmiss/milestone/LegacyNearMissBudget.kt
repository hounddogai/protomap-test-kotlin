package com.acme.transport.safety.nearmiss.milestone

import com.acme.transport.kit.text.normalize

/** Where a legacy near miss budget is in its life cycle. */
enum class LegacyNearMissBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A legacy near miss budget. Its code is unique within a tenant. */
data class LegacyNearMissBudget(
    var code: String,
    var label: String,
    var approved: Boolean,
    var graceDays: Int,
    var externalRef: String,
    var status: LegacyNearMissBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyNearMissBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyNearMissBudgetStatus): Boolean {
        when (status) {
            LegacyNearMissBudgetStatus.REQUESTED -> {
                when (next) {
                    LegacyNearMissBudgetStatus.GRANTED -> return true
                }
                return false
            }
            LegacyNearMissBudgetStatus.GRANTED -> return next == LegacyNearMissBudgetStatus.REVOKED
            LegacyNearMissBudgetStatus.REVOKED -> return next == LegacyNearMissBudgetStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LegacyNearMissBudgetStatus) {
        if (!canMoveTo(next)) {
            error("legacy near miss budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): LegacyNearMissBudget {
        return LegacyNearMissBudget(
            code = code,
            label = label,
            approved = approved,
            graceDays = graceDays,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy near miss budget in status requested. */
fun newLegacyNearMissBudget(code: String, label: String): LegacyNearMissBudget {
    return LegacyNearMissBudget(
        code = code,
        label = label,
        approved = false,
        graceDays = 0,
        externalRef = "",
        status = LegacyNearMissBudgetStatus.REQUESTED,
    )
}
