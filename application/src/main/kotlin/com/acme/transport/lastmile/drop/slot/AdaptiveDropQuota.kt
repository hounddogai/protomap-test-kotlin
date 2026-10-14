package com.acme.transport.lastmile.drop.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive drop quota is in its life cycle. */
enum class AdaptiveDropQuotaStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A adaptive drop quota. Its code is unique within a tenant. */
data class AdaptiveDropQuota(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var loadKg: Double,
    var driverId: String,
    var reviewer: String,
    var contactEmail: String,
    var timezone: String,
    var pinned: Boolean,
    var startsAt: Int,
    var status: AdaptiveDropQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDropQuotaStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDropQuotaStatus): Boolean {
        when (status) {
            AdaptiveDropQuotaStatus.PLANNED -> return next == AdaptiveDropQuotaStatus.IN_PROGRESS
            AdaptiveDropQuotaStatus.SCHEDULED -> return next == AdaptiveDropQuotaStatus.CANCELLED
            AdaptiveDropQuotaStatus.IN_PROGRESS -> {
                when (next) {
                    AdaptiveDropQuotaStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveDropQuotaStatus.COMPLETED -> return next == AdaptiveDropQuotaStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AdaptiveDropQuotaStatus) {
        if (!canMoveTo(next)) {
            error("adaptive drop quota $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netValue(): Double = round(expiresAt.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): AdaptiveDropQuota {
        return AdaptiveDropQuota(
            code = code,
            title = title,
            expiresAt = expiresAt,
            loadKg = loadKg,
            driverId = driverId,
            reviewer = reviewer,
            contactEmail = contactEmail,
            timezone = timezone,
            pinned = pinned,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive drop quota in status planned. */
fun newAdaptiveDropQuota(code: String, title: String): AdaptiveDropQuota {
    return AdaptiveDropQuota(
        code = code,
        title = title,
        expiresAt = 0,
        loadKg = 0.0,
        driverId = "",
        reviewer = "",
        contactEmail = "",
        timezone = "",
        pinned = false,
        startsAt = 0,
        status = AdaptiveDropQuotaStatus.PLANNED,
    )
}
