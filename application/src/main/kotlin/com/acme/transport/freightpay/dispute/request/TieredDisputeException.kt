package com.acme.transport.freightpay.dispute.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered dispute exception is in its life cycle. */
enum class TieredDisputeExceptionStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a tiered dispute exception and its current status. */
data class TieredDisputeException(
    var code: String,
    var name: String,
    var cost: Double,
    var createdAt: Int,
    var amount: Double,
    var carrier: String,
    var endsAt: Int,
    var dock: String,
    var unit: String,
    var status: TieredDisputeExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDisputeExceptionStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDisputeExceptionStatus): Boolean {
        when (status) {
            TieredDisputeExceptionStatus.OPEN -> return next == TieredDisputeExceptionStatus.IN_REVIEW
            TieredDisputeExceptionStatus.IN_REVIEW -> {
                when (next) {
                    TieredDisputeExceptionStatus.RESOLVED, TieredDisputeExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredDisputeExceptionStatus.RESOLVED -> {
                when (next) {
                    TieredDisputeExceptionStatus.REOPENED, TieredDisputeExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredDisputeExceptionStatus.REOPENED -> return next == TieredDisputeExceptionStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredDisputeExceptionStatus) {
        if (!canMoveTo(next)) {
            error("tiered dispute exception $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveExposure(): Double = round((endsAt.toDouble() - amount) * 100) / 100

    fun snapshot(): TieredDisputeException {
        return TieredDisputeException(
            code = code,
            name = name,
            cost = cost,
            createdAt = createdAt,
            amount = amount,
            carrier = carrier,
            endsAt = endsAt,
            dock = dock,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a tiered dispute exception in status open. */
fun newTieredDisputeException(code: String, name: String): TieredDisputeException {
    return TieredDisputeException(
        code = code,
        name = name,
        cost = 0.0,
        createdAt = 0,
        amount = 0.0,
        carrier = "",
        endsAt = 0,
        dock = "",
        unit = "",
        status = TieredDisputeExceptionStatus.OPEN,
    )
}
