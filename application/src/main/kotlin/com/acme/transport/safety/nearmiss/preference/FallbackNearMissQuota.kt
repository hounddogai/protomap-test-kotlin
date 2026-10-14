package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback near miss quota is in its life cycle. */
enum class FallbackNearMissQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fallback near miss quota as the near miss team keeps it. */
data class FallbackNearMissQuota(
    var code: String,
    var label: String,
    var reviewer: String,
    var odometer: Int,
    var title: String,
    var locale: String,
    var rate: Double,
    var ceiling: Double,
    var discount: Double,
    var ratio: Double,
    var status: FallbackNearMissQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackNearMissQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackNearMissQuotaStatus): Boolean {
        when (status) {
            FallbackNearMissQuotaStatus.NEW -> return next == FallbackNearMissQuotaStatus.QUEUED
            FallbackNearMissQuotaStatus.QUEUED -> return next == FallbackNearMissQuotaStatus.RUNNING || next == FallbackNearMissQuotaStatus.DONE
            FallbackNearMissQuotaStatus.RUNNING -> return next == FallbackNearMissQuotaStatus.FAILED
            FallbackNearMissQuotaStatus.DONE -> {
                when (next) {
                    FallbackNearMissQuotaStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackNearMissQuotaStatus) {
        if (!canMoveTo(next)) {
            error("fallback near miss quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedLoad(): Double = round((ceiling + ratio) * 100) / 100

    fun snapshot(): FallbackNearMissQuota {
        return FallbackNearMissQuota(
            code = code,
            label = label,
            reviewer = reviewer,
            odometer = odometer,
            title = title,
            locale = locale,
            rate = rate,
            ceiling = ceiling,
            discount = discount,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback near miss quota in status new. */
fun newFallbackNearMissQuota(code: String, label: String): FallbackNearMissQuota {
    return FallbackNearMissQuota(
        code = code,
        label = label,
        reviewer = "",
        odometer = 0,
        title = "",
        locale = "",
        rate = 0.0,
        ceiling = 0.0,
        discount = 0.0,
        ratio = 0.0,
        status = FallbackNearMissQuotaStatus.NEW,
    )
}
