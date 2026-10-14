package com.acme.transport.freightpay.audit.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit window is in its life cycle. */
enum class AuditWindowStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A audit window as the audit team keeps it. */
data class AuditWindow(
    var code: String,
    var title: String,
    var budget: Double,
    var pinned: Boolean,
    var startsAt: Int,
    var batchSize: Int,
    var score: Double,
    var pallets: Int,
    var status: AuditWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditWindowStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditWindowStatus): Boolean {
        when (status) {
            AuditWindowStatus.PROPOSED -> return next == AuditWindowStatus.WITHDRAWN
            AuditWindowStatus.APPROVED -> {
                when (next) {
                    AuditWindowStatus.WITHDRAWN -> return true
                }
                return false
            }
            AuditWindowStatus.REJECTED -> return next == AuditWindowStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AuditWindowStatus) {
        if (!canMoveTo(next)) {
            error("audit window $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedValue(): Double = round(startsAt.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): AuditWindow {
        return AuditWindow(
            code = code,
            title = title,
            budget = budget,
            pinned = pinned,
            startsAt = startsAt,
            batchSize = batchSize,
            score = score,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a audit window in status proposed. */
fun newAuditWindow(code: String, title: String): AuditWindow {
    return AuditWindow(
        code = code,
        title = title,
        budget = 0.0,
        pinned = false,
        startsAt = 0,
        batchSize = 0,
        score = 0.0,
        pallets = 0,
        status = AuditWindowStatus.PROPOSED,
    )
}
