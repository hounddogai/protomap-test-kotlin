package com.acme.transport.freightpay.accessorial.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The accessorial quota records of freightpay's accessorial work, and the rules, stores, and calculations
// around them.

/** Where a accessorial quota is in its life cycle. */
enum class AccessorialQuotaStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A accessorial quota as the accessorial team keeps it. */
data class AccessorialQuota(
    var code: String,
    var title: String,
    var segment: String,
    var driverId: String,
    var distanceKm: Double,
    var summary: String,
    var archived: Boolean,
    var amount: Double,
    var expiresAt: Int,
    var notes: String,
    var status: AccessorialQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialQuotaStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialQuotaStatus): Boolean {
        when (status) {
            AccessorialQuotaStatus.OPEN -> return next == AccessorialQuotaStatus.RESOLVED
            AccessorialQuotaStatus.IN_REVIEW -> return next == AccessorialQuotaStatus.REOPENED
            AccessorialQuotaStatus.RESOLVED -> {
                when (next) {
                    AccessorialQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
            AccessorialQuotaStatus.REOPENED -> return next == AccessorialQuotaStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AccessorialQuotaStatus) {
        if (!canMoveTo(next)) {
            error("accessorial quota $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun weightedTotal(): Double = round((amount + distanceKm) * 100) / 100

    fun snapshot(): AccessorialQuota {
        return AccessorialQuota(
            code = code,
            title = title,
            segment = segment,
            driverId = driverId,
            distanceKm = distanceKm,
            summary = summary,
            archived = archived,
            amount = amount,
            expiresAt = expiresAt,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a accessorial quota in status open. */
fun newAccessorialQuota(code: String, title: String): AccessorialQuota {
    return AccessorialQuota(
        code = code,
        title = title,
        segment = "",
        driverId = "",
        distanceKm = 0.0,
        summary = "",
        archived = false,
        amount = 0.0,
        expiresAt = 0,
        notes = "",
        status = AccessorialQuotaStatus.OPEN,
    )
}
