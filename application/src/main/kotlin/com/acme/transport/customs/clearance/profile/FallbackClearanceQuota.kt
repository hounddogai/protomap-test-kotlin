package com.acme.transport.customs.clearance.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback clearance quota is in its life cycle. */
enum class FallbackClearanceQuotaStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One fallback clearance quota, with what the planners need to act on it. */
data class FallbackClearanceQuota(
    var code: String,
    var label: String,
    var ceiling: Double,
    var etaMinutes: Int,
    var stopCount: Int,
    var status: FallbackClearanceQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackClearanceQuotaStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackClearanceQuotaStatus): Boolean {
        when (status) {
            FallbackClearanceQuotaStatus.REQUESTED -> return next == FallbackClearanceQuotaStatus.REVOKED || next == FallbackClearanceQuotaStatus.EXPIRED
            FallbackClearanceQuotaStatus.GRANTED -> {
                when (next) {
                    FallbackClearanceQuotaStatus.REVOKED -> return true
                }
                return false
            }
            FallbackClearanceQuotaStatus.REVOKED -> {
                when (next) {
                    FallbackClearanceQuotaStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackClearanceQuotaStatus) {
        if (!canMoveTo(next)) {
            error("fallback clearance quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fallback clearance quota"} $code"

    fun effectiveValue(): Double = round((stopCount.toDouble() - ceiling) * 100) / 100

    fun snapshot(): FallbackClearanceQuota {
        return FallbackClearanceQuota(
            code = code,
            label = label,
            ceiling = ceiling,
            etaMinutes = etaMinutes,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback clearance quota in status requested. */
fun newFallbackClearanceQuota(code: String, label: String): FallbackClearanceQuota {
    return FallbackClearanceQuota(
        code = code,
        label = label,
        ceiling = 0.0,
        etaMinutes = 0,
        stopCount = 0,
        status = FallbackClearanceQuotaStatus.REQUESTED,
    )
}
