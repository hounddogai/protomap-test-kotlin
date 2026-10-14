package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual attempt quota is in its life cycle. */
enum class ManualAttemptQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One manual attempt quota, with what the planners need to act on it. */
data class ManualAttemptQuota(
    var code: String,
    var title: String,
    var floorValue: Double,
    var budget: Double,
    var threshold: Double,
    var retries: Int,
    var ownerId: String,
    var segment: String,
    var status: ManualAttemptQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAttemptQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAttemptQuotaStatus): Boolean {
        when (status) {
            ManualAttemptQuotaStatus.DRAFT -> {
                when (next) {
                    ManualAttemptQuotaStatus.ACTIVE -> return true
                }
                return false
            }
            ManualAttemptQuotaStatus.PENDING -> return next == ManualAttemptQuotaStatus.ACTIVE
            ManualAttemptQuotaStatus.ACTIVE -> return next == ManualAttemptQuotaStatus.CLOSED
            ManualAttemptQuotaStatus.SUSPENDED -> return next == ManualAttemptQuotaStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ManualAttemptQuotaStatus) {
        if (!canMoveTo(next)) {
            error("manual attempt quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"manual attempt quota"} $code"

    fun effectiveLoad(): Double = round((threshold + budget) * 100) / 100

    fun snapshot(): ManualAttemptQuota {
        return ManualAttemptQuota(
            code = code,
            title = title,
            floorValue = floorValue,
            budget = budget,
            threshold = threshold,
            retries = retries,
            ownerId = ownerId,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a manual attempt quota in status draft. */
fun newManualAttemptQuota(code: String, title: String): ManualAttemptQuota {
    return ManualAttemptQuota(
        code = code,
        title = title,
        floorValue = 0.0,
        budget = 0.0,
        threshold = 0.0,
        retries = 0,
        ownerId = "",
        segment = "",
        status = ManualAttemptQuotaStatus.DRAFT,
    )
}
