package com.acme.transport.freightpay.audit.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit profile is in its life cycle. */
enum class AuditProfileStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A audit profile as the audit team keeps it. */
data class AuditProfile(
    var code: String,
    var name: String,
    var version: Int,
    var expiresAt: Int,
    var startsAt: Int,
    var floorValue: Double,
    var depot: String,
    var status: AuditProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditProfileStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditProfileStatus): Boolean {
        when (status) {
            AuditProfileStatus.OPEN -> {
                when (next) {
                    AuditProfileStatus.IN_REVIEW -> return true
                }
                return false
            }
            AuditProfileStatus.IN_REVIEW -> {
                when (next) {
                    AuditProfileStatus.RESOLVED -> return true
                }
                return false
            }
            AuditProfileStatus.RESOLVED -> return next == AuditProfileStatus.REOPENED
            AuditProfileStatus.REOPENED -> return next == AuditProfileStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AuditProfileStatus) {
        if (!canMoveTo(next)) {
            error("audit profile $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"audit profile"} $code"

    fun projectedTotal(): Double = round(expiresAt.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): AuditProfile {
        return AuditProfile(
            code = code,
            name = name,
            version = version,
            expiresAt = expiresAt,
            startsAt = startsAt,
            floorValue = floorValue,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a audit profile in status open. */
fun newAuditProfile(code: String, name: String): AuditProfile {
    return AuditProfile(
        code = code,
        name = name,
        version = 0,
        expiresAt = 0,
        startsAt = 0,
        floorValue = 0.0,
        depot = "",
        status = AuditProfileStatus.OPEN,
    )
}
