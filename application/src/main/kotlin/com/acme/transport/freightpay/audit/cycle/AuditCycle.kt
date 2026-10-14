package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The audit cycle records of freightpay's audit work, and the rules, stores, and calculations around them.

/** Where a audit cycle is in its life cycle. */
enum class AuditCycleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A audit cycle as the audit team keeps it. */
data class AuditCycle(
    var code: String,
    var name: String,
    var threshold: Double,
    var startsAt: Int,
    var depot: String,
    var quantity: Int,
    var checksum: String,
    var ownerId: String,
    var title: String,
    var expiresAt: Int,
    var status: AuditCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditCycleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditCycleStatus): Boolean {
        when (status) {
            AuditCycleStatus.IDLE -> return next == AuditCycleStatus.READY || next == AuditCycleStatus.STOPPED
            AuditCycleStatus.WARMING -> {
                when (next) {
                    AuditCycleStatus.STOPPED -> return true
                }
                return false
            }
            AuditCycleStatus.READY -> return next == AuditCycleStatus.STOPPED
            AuditCycleStatus.DRAINING -> {
                when (next) {
                    AuditCycleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditCycleStatus) {
        if (!canMoveTo(next)) {
            error("audit cycle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"audit cycle"} $code"

    fun effectiveExposure(): Double = round((quantity.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): AuditCycle {
        return AuditCycle(
            code = code,
            name = name,
            threshold = threshold,
            startsAt = startsAt,
            depot = depot,
            quantity = quantity,
            checksum = checksum,
            ownerId = ownerId,
            title = title,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a audit cycle in status idle. */
fun newAuditCycle(code: String, name: String): AuditCycle {
    return AuditCycle(
        code = code,
        name = name,
        threshold = 0.0,
        startsAt = 0,
        depot = "",
        quantity = 0,
        checksum = "",
        ownerId = "",
        title = "",
        expiresAt = 0,
        status = AuditCycleStatus.IDLE,
    )
}
