package com.acme.transport.linehaul.backhaul.target

import com.acme.transport.kit.text.normalize

/** Where a capped backhaul policy is in its life cycle. */
enum class CappedBackhaulPolicyStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a capped backhaul policy and its current status. */
data class CappedBackhaulPolicy(
    var code: String,
    var title: String,
    var budget: Double,
    var locale: String,
    var shiftCode: String,
    var status: CappedBackhaulPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedBackhaulPolicyStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedBackhaulPolicyStatus): Boolean {
        when (status) {
            CappedBackhaulPolicyStatus.DRAFT -> return next == CappedBackhaulPolicyStatus.PENDING
            CappedBackhaulPolicyStatus.PENDING -> {
                when (next) {
                    CappedBackhaulPolicyStatus.ACTIVE, CappedBackhaulPolicyStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedBackhaulPolicyStatus.ACTIVE -> return next == CappedBackhaulPolicyStatus.CLOSED
            CappedBackhaulPolicyStatus.SUSPENDED -> return next == CappedBackhaulPolicyStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedBackhaulPolicyStatus) {
        if (!canMoveTo(next)) {
            error("capped backhaul policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = budget

    fun snapshot(): CappedBackhaulPolicy {
        return CappedBackhaulPolicy(
            code = code,
            title = title,
            budget = budget,
            locale = locale,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a capped backhaul policy in status draft. */
fun newCappedBackhaulPolicy(code: String, title: String): CappedBackhaulPolicy {
    return CappedBackhaulPolicy(
        code = code,
        title = title,
        budget = 0.0,
        locale = "",
        shiftCode = "",
        status = CappedBackhaulPolicyStatus.DRAFT,
    )
}
