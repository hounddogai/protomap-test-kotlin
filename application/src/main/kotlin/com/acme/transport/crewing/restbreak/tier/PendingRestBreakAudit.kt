package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending rest break audit is in its life cycle. */
enum class PendingRestBreakAuditStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A pending rest break audit. Its code is unique within a tenant. */
data class PendingRestBreakAudit(
    var code: String,
    var title: String,
    var summary: String,
    var discount: Double,
    var graceDays: Int,
    var batchSize: Int,
    var description: String,
    var trailerId: String,
    var odometer: Int,
    var status: PendingRestBreakAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRestBreakAuditStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRestBreakAuditStatus): Boolean {
        when (status) {
            PendingRestBreakAuditStatus.IDLE -> {
                when (next) {
                    PendingRestBreakAuditStatus.WARMING, PendingRestBreakAuditStatus.STOPPED -> return true
                }
                return false
            }
            PendingRestBreakAuditStatus.WARMING -> return next == PendingRestBreakAuditStatus.READY
            PendingRestBreakAuditStatus.READY -> {
                when (next) {
                    PendingRestBreakAuditStatus.STOPPED -> return true
                }
                return false
            }
            PendingRestBreakAuditStatus.DRAINING -> {
                when (next) {
                    PendingRestBreakAuditStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingRestBreakAuditStatus) {
        if (!canMoveTo(next)) {
            error("pending rest break audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending rest break audit"} $code"

    fun adjustedValue(): Double = round((odometer.toDouble() + discount) * 100) / 100

    fun snapshot(): PendingRestBreakAudit {
        return PendingRestBreakAudit(
            code = code,
            title = title,
            summary = summary,
            discount = discount,
            graceDays = graceDays,
            batchSize = batchSize,
            description = description,
            trailerId = trailerId,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending rest break audit in status idle. */
fun newPendingRestBreakAudit(code: String, title: String): PendingRestBreakAudit {
    return PendingRestBreakAudit(
        code = code,
        title = title,
        summary = "",
        discount = 0.0,
        graceDays = 0,
        batchSize = 0,
        description = "",
        trailerId = "",
        odometer = 0,
        status = PendingRestBreakAuditStatus.IDLE,
    )
}
