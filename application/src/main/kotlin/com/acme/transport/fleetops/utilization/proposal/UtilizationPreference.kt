package com.acme.transport.fleetops.utilization.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization preference is in its life cycle. */
enum class UtilizationPreferenceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A utilization preference. Its code is unique within a tenant. */
data class UtilizationPreference(
    var code: String,
    var title: String,
    var graceDays: Int,
    var locale: String,
    var checksum: String,
    var ratio: Double,
    var currency: String,
    var createdAt: Int,
    var reviewer: String,
    var pinned: Boolean,
    var timezone: String,
    var status: UtilizationPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationPreferenceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationPreferenceStatus): Boolean {
        when (status) {
            UtilizationPreferenceStatus.PROPOSED -> return next == UtilizationPreferenceStatus.REJECTED || next == UtilizationPreferenceStatus.WITHDRAWN
            UtilizationPreferenceStatus.APPROVED -> {
                when (next) {
                    UtilizationPreferenceStatus.REJECTED -> return true
                }
                return false
            }
            UtilizationPreferenceStatus.REJECTED -> return next == UtilizationPreferenceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: UtilizationPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("utilization preference $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"utilization preference"} $code"

    fun netValue(): Double = round((ratio + graceDays.toDouble()) * 100) / 100

    fun snapshot(): UtilizationPreference {
        return UtilizationPreference(
            code = code,
            title = title,
            graceDays = graceDays,
            locale = locale,
            checksum = checksum,
            ratio = ratio,
            currency = currency,
            createdAt = createdAt,
            reviewer = reviewer,
            pinned = pinned,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a utilization preference in status proposed. */
fun newUtilizationPreference(code: String, title: String): UtilizationPreference {
    return UtilizationPreference(
        code = code,
        title = title,
        graceDays = 0,
        locale = "",
        checksum = "",
        ratio = 0.0,
        currency = "",
        createdAt = 0,
        reviewer = "",
        pinned = false,
        timezone = "",
        status = UtilizationPreferenceStatus.PROPOSED,
    )
}
