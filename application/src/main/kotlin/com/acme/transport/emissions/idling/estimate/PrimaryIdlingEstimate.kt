package com.acme.transport.emissions.idling.estimate

import com.acme.transport.kit.text.normalize

// The primary idling estimate records of emissions's idling work, and the rules, stores, and calculations
// around them.

/** Where a primary idling estimate is in its life cycle. */
enum class PrimaryIdlingEstimateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A primary idling estimate. Its code is unique within a tenant. */
data class PrimaryIdlingEstimate(
    var code: String,
    var label: String,
    var currency: String,
    var externalRef: String,
    var amount: Double,
    var carrier: String,
    var status: PrimaryIdlingEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryIdlingEstimateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryIdlingEstimateStatus): Boolean {
        when (status) {
            PrimaryIdlingEstimateStatus.PROPOSED -> {
                when (next) {
                    PrimaryIdlingEstimateStatus.APPROVED, PrimaryIdlingEstimateStatus.REJECTED -> return true
                }
                return false
            }
            PrimaryIdlingEstimateStatus.APPROVED -> return next == PrimaryIdlingEstimateStatus.REJECTED || next == PrimaryIdlingEstimateStatus.WITHDRAWN
            PrimaryIdlingEstimateStatus.REJECTED -> return next == PrimaryIdlingEstimateStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PrimaryIdlingEstimateStatus) {
        if (!canMoveTo(next)) {
            error("primary idling estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary idling estimate"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): PrimaryIdlingEstimate {
        return PrimaryIdlingEstimate(
            code = code,
            label = label,
            currency = currency,
            externalRef = externalRef,
            amount = amount,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary idling estimate in status proposed. */
fun newPrimaryIdlingEstimate(code: String, label: String): PrimaryIdlingEstimate {
    return PrimaryIdlingEstimate(
        code = code,
        label = label,
        currency = "",
        externalRef = "",
        amount = 0.0,
        carrier = "",
        status = PrimaryIdlingEstimateStatus.PROPOSED,
    )
}
