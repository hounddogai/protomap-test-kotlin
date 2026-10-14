package com.acme.transport.freightpay.dispute.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute batch is in its life cycle. */
enum class DisputeBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A dispute batch. Its code is unique within a tenant. */
data class DisputeBatch(
    var code: String,
    var title: String,
    var attempts: Int,
    var routeCode: String,
    var ceiling: Double,
    var label: String,
    var cost: Double,
    var approved: Boolean,
    var status: DisputeBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeBatchStatus): Boolean {
        when (status) {
            DisputeBatchStatus.OPEN -> {
                when (next) {
                    DisputeBatchStatus.RESOLVED, DisputeBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            DisputeBatchStatus.IN_REVIEW -> {
                when (next) {
                    DisputeBatchStatus.RESOLVED, DisputeBatchStatus.REOPENED -> return true
                }
                return false
            }
            DisputeBatchStatus.RESOLVED -> {
                when (next) {
                    DisputeBatchStatus.REOPENED -> return true
                }
                return false
            }
            DisputeBatchStatus.REOPENED -> {
                when (next) {
                    DisputeBatchStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeBatchStatus) {
        if (!canMoveTo(next)) {
            error("dispute batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"dispute batch"} $code"

    fun weightedExposure(): Double = round(cost * attempts.toDouble() * 100) / 100

    fun snapshot(): DisputeBatch {
        return DisputeBatch(
            code = code,
            title = title,
            attempts = attempts,
            routeCode = routeCode,
            ceiling = ceiling,
            label = label,
            cost = cost,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a dispute batch in status open. */
fun newDisputeBatch(code: String, title: String): DisputeBatch {
    return DisputeBatch(
        code = code,
        title = title,
        attempts = 0,
        routeCode = "",
        ceiling = 0.0,
        label = "",
        cost = 0.0,
        approved = false,
        status = DisputeBatchStatus.OPEN,
    )
}
