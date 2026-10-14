package com.acme.transport.fleetops.tire.tireagreement

import com.acme.transport.kit.text.normalize

// The pending tire agreement records of fleetops's tire work, and the rules, stores, and calculations around
// them.

/** Where a pending tire agreement is in its life cycle. */
enum class PendingTireAgreementStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A pending tire agreement. Its code is unique within a tenant. */
data class PendingTireAgreement(
    var code: String,
    var title: String,
    var lane: String,
    var category: String,
    var reviewer: String,
    var minimum: Double,
    var status: PendingTireAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTireAgreementStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTireAgreementStatus): Boolean {
        when (status) {
            PendingTireAgreementStatus.DRAFT -> return next == PendingTireAgreementStatus.CLOSED
            PendingTireAgreementStatus.PENDING -> {
                when (next) {
                    PendingTireAgreementStatus.SUSPENDED, PendingTireAgreementStatus.CLOSED -> return true
                }
                return false
            }
            PendingTireAgreementStatus.ACTIVE -> {
                when (next) {
                    PendingTireAgreementStatus.SUSPENDED -> return true
                }
                return false
            }
            PendingTireAgreementStatus.SUSPENDED -> return next == PendingTireAgreementStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PendingTireAgreementStatus) {
        if (!canMoveTo(next)) {
            error("pending tire agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = minimum

    fun snapshot(): PendingTireAgreement {
        return PendingTireAgreement(
            code = code,
            title = title,
            lane = lane,
            category = category,
            reviewer = reviewer,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a pending tire agreement in status draft. */
fun newPendingTireAgreement(code: String, title: String): PendingTireAgreement {
    return PendingTireAgreement(
        code = code,
        title = title,
        lane = "",
        category = "",
        reviewer = "",
        minimum = 0.0,
        status = PendingTireAgreementStatus.DRAFT,
    )
}
