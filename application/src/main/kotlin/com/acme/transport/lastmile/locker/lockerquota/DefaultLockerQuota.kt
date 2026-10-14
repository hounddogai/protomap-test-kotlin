package com.acme.transport.lastmile.locker.lockerquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default locker quota records of lastmile's locker work, and the rules, stores, and calculations around
// them.

/** Where a default locker quota is in its life cycle. */
enum class DefaultLockerQuotaStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One default locker quota, with what the planners need to act on it. */
data class DefaultLockerQuota(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var windowDays: Int,
    var priority: Int,
    var odometer: Int,
    var status: DefaultLockerQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultLockerQuotaStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultLockerQuotaStatus): Boolean {
        when (status) {
            DefaultLockerQuotaStatus.PLANNED -> {
                when (next) {
                    DefaultLockerQuotaStatus.COMPLETED, DefaultLockerQuotaStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultLockerQuotaStatus.SCHEDULED -> return next == DefaultLockerQuotaStatus.IN_PROGRESS
            DefaultLockerQuotaStatus.IN_PROGRESS -> return next == DefaultLockerQuotaStatus.COMPLETED || next == DefaultLockerQuotaStatus.CANCELLED
            DefaultLockerQuotaStatus.COMPLETED -> return next == DefaultLockerQuotaStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DefaultLockerQuotaStatus) {
        if (!canMoveTo(next)) {
            error("default locker quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default locker quota"} $code"

    fun adjustedTotal(): Double = round((etaMinutes.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): DefaultLockerQuota {
        return DefaultLockerQuota(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            windowDays = windowDays,
            priority = priority,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default locker quota in status planned. */
fun newDefaultLockerQuota(code: String, title: String): DefaultLockerQuota {
    return DefaultLockerQuota(
        code = code,
        title = title,
        etaMinutes = 0,
        windowDays = 0,
        priority = 0,
        odometer = 0,
        status = DefaultLockerQuotaStatus.PLANNED,
    )
}
