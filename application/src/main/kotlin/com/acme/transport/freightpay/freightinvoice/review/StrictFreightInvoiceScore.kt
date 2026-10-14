package com.acme.transport.freightpay.freightinvoice.review

import com.acme.transport.kit.text.normalize

/** Where a strict freight invoice score is in its life cycle. */
enum class StrictFreightInvoiceScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One strict freight invoice score, with what the planners need to act on it. */
data class StrictFreightInvoiceScore(
    var code: String,
    var title: String,
    var ownerId: String,
    var region: String,
    var carrier: String,
    var shiftCode: String,
    var depot: String,
    var updatedAt: Int,
    var status: StrictFreightInvoiceScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictFreightInvoiceScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictFreightInvoiceScoreStatus): Boolean {
        when (status) {
            StrictFreightInvoiceScoreStatus.PLANNED -> return next == StrictFreightInvoiceScoreStatus.COMPLETED || next == StrictFreightInvoiceScoreStatus.CANCELLED
            StrictFreightInvoiceScoreStatus.SCHEDULED -> {
                when (next) {
                    StrictFreightInvoiceScoreStatus.IN_PROGRESS, StrictFreightInvoiceScoreStatus.CANCELLED -> return true
                }
                return false
            }
            StrictFreightInvoiceScoreStatus.IN_PROGRESS -> return next == StrictFreightInvoiceScoreStatus.COMPLETED
            StrictFreightInvoiceScoreStatus.COMPLETED -> {
                when (next) {
                    StrictFreightInvoiceScoreStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictFreightInvoiceScoreStatus) {
        if (!canMoveTo(next)) {
            error("strict freight invoice score $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = updatedAt.toDouble()

    fun snapshot(): StrictFreightInvoiceScore {
        return StrictFreightInvoiceScore(
            code = code,
            title = title,
            ownerId = ownerId,
            region = region,
            carrier = carrier,
            shiftCode = shiftCode,
            depot = depot,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a strict freight invoice score in status planned. */
fun newStrictFreightInvoiceScore(code: String, title: String): StrictFreightInvoiceScore {
    return StrictFreightInvoiceScore(
        code = code,
        title = title,
        ownerId = "",
        region = "",
        carrier = "",
        shiftCode = "",
        depot = "",
        updatedAt = 0,
        status = StrictFreightInvoiceScoreStatus.PLANNED,
    )
}
