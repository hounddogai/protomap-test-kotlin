package com.acme.transport.fleetops.tire.tirewindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default tire quota is in its life cycle. */
enum class DefaultTireQuotaStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One default tire quota, with what the planners need to act on it. */
data class DefaultTireQuota(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var segment: String,
    var retries: Int,
    var ownerId: String,
    var timezone: String,
    var approved: Boolean,
    var ratio: Double,
    var expiresAt: Int,
    var status: DefaultTireQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTireQuotaStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTireQuotaStatus): Boolean {
        when (status) {
            DefaultTireQuotaStatus.PROPOSED -> return next == DefaultTireQuotaStatus.REJECTED || next == DefaultTireQuotaStatus.WITHDRAWN
            DefaultTireQuotaStatus.APPROVED -> return next == DefaultTireQuotaStatus.REJECTED || next == DefaultTireQuotaStatus.WITHDRAWN
            DefaultTireQuotaStatus.REJECTED -> return next == DefaultTireQuotaStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DefaultTireQuotaStatus) {
        if (!canMoveTo(next)) {
            error("default tire quota $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"default tire quota"} $code"

    fun effectiveTotal(): Double = round((etaMinutes.toDouble() + ratio) * 100) / 100

    fun snapshot(): DefaultTireQuota {
        return DefaultTireQuota(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            segment = segment,
            retries = retries,
            ownerId = ownerId,
            timezone = timezone,
            approved = approved,
            ratio = ratio,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a default tire quota in status proposed. */
fun newDefaultTireQuota(code: String, label: String): DefaultTireQuota {
    return DefaultTireQuota(
        code = code,
        label = label,
        etaMinutes = 0,
        segment = "",
        retries = 0,
        ownerId = "",
        timezone = "",
        approved = false,
        ratio = 0.0,
        expiresAt = 0,
        status = DefaultTireQuotaStatus.PROPOSED,
    )
}
