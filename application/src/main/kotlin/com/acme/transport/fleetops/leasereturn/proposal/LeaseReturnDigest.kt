package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return digest is in its life cycle. */
enum class LeaseReturnDigestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a lease return digest and its current status. */
data class LeaseReturnDigest(
    var code: String,
    var name: String,
    var priority: Int,
    var threshold: Double,
    var durationMinutes: Int,
    var discount: Double,
    var endsAt: Int,
    var contactEmail: String,
    var locale: String,
    var routeCode: String,
    var status: LeaseReturnDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnDigestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnDigestStatus): Boolean {
        when (status) {
            LeaseReturnDigestStatus.OPEN -> return next == LeaseReturnDigestStatus.ARCHIVED
            LeaseReturnDigestStatus.IN_REVIEW -> {
                when (next) {
                    LeaseReturnDigestStatus.ARCHIVED -> return true
                }
                return false
            }
            LeaseReturnDigestStatus.RESOLVED -> return next == LeaseReturnDigestStatus.ARCHIVED
            LeaseReturnDigestStatus.REOPENED -> {
                when (next) {
                    LeaseReturnDigestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnDigestStatus) {
        if (!canMoveTo(next)) {
            error("lease return digest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lease return digest"} $code"

    fun weightedVolume(): Double = round((endsAt.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): LeaseReturnDigest {
        return LeaseReturnDigest(
            code = code,
            name = name,
            priority = priority,
            threshold = threshold,
            durationMinutes = durationMinutes,
            discount = discount,
            endsAt = endsAt,
            contactEmail = contactEmail,
            locale = locale,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a lease return digest in status open. */
fun newLeaseReturnDigest(code: String, name: String): LeaseReturnDigest {
    return LeaseReturnDigest(
        code = code,
        name = name,
        priority = 0,
        threshold = 0.0,
        durationMinutes = 0,
        discount = 0.0,
        endsAt = 0,
        contactEmail = "",
        locale = "",
        routeCode = "",
        status = LeaseReturnDigestStatus.OPEN,
    )
}
