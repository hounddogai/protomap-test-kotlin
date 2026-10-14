package com.acme.transport.lastmile.attempt.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt audit is in its life cycle. */
enum class AttemptAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A attempt audit as the attempt team keeps it. */
data class AttemptAudit(
    var code: String,
    var title: String,
    var timezone: String,
    var windowDays: Int,
    var driverId: String,
    var pallets: Int,
    var fuelLiters: Double,
    var name: String,
    var archived: Boolean,
    var amount: Double,
    var priority: Int,
    var status: AttemptAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptAuditStatus): Boolean {
        when (status) {
            AttemptAuditStatus.OPEN -> {
                when (next) {
                    AttemptAuditStatus.RESOLVED, AttemptAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            AttemptAuditStatus.IN_REVIEW -> return next == AttemptAuditStatus.RESOLVED || next == AttemptAuditStatus.ARCHIVED
            AttemptAuditStatus.RESOLVED -> {
                when (next) {
                    AttemptAuditStatus.REOPENED, AttemptAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            AttemptAuditStatus.REOPENED -> return next == AttemptAuditStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AttemptAuditStatus) {
        if (!canMoveTo(next)) {
            error("attempt audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"attempt audit"} $code"

    fun weightedTotal(): Double = round((pallets.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): AttemptAudit {
        return AttemptAudit(
            code = code,
            title = title,
            timezone = timezone,
            windowDays = windowDays,
            driverId = driverId,
            pallets = pallets,
            fuelLiters = fuelLiters,
            name = name,
            archived = archived,
            amount = amount,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a attempt audit in status open. */
fun newAttemptAudit(code: String, title: String): AttemptAudit {
    return AttemptAudit(
        code = code,
        title = title,
        timezone = "",
        windowDays = 0,
        driverId = "",
        pallets = 0,
        fuelLiters = 0.0,
        name = "",
        archived = false,
        amount = 0.0,
        priority = 0,
        status = AttemptAuditStatus.OPEN,
    )
}
