package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.kit.text.normalize

/** Where a strict cross dock agreement is in its life cycle. */
enum class StrictCrossDockAgreementStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A strict cross dock agreement. Its code is unique within a tenant. */
data class StrictCrossDockAgreement(
    var code: String,
    var title: String,
    var lane: String,
    var currency: String,
    var priority: Int,
    var status: StrictCrossDockAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictCrossDockAgreementStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictCrossDockAgreementStatus): Boolean {
        when (status) {
            StrictCrossDockAgreementStatus.DRAFT -> return next == StrictCrossDockAgreementStatus.SUSPENDED
            StrictCrossDockAgreementStatus.PENDING -> {
                when (next) {
                    StrictCrossDockAgreementStatus.ACTIVE -> return true
                }
                return false
            }
            StrictCrossDockAgreementStatus.ACTIVE -> return next == StrictCrossDockAgreementStatus.CLOSED
            StrictCrossDockAgreementStatus.SUSPENDED -> {
                when (next) {
                    StrictCrossDockAgreementStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictCrossDockAgreementStatus) {
        if (!canMoveTo(next)) {
            error("strict cross dock agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): StrictCrossDockAgreement {
        return StrictCrossDockAgreement(
            code = code,
            title = title,
            lane = lane,
            currency = currency,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a strict cross dock agreement in status draft. */
fun newStrictCrossDockAgreement(code: String, title: String): StrictCrossDockAgreement {
    return StrictCrossDockAgreement(
        code = code,
        title = title,
        lane = "",
        currency = "",
        priority = 0,
        status = StrictCrossDockAgreementStatus.DRAFT,
    )
}
