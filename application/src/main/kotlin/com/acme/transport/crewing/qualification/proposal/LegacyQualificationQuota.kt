package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy qualification quota is in its life cycle. */
enum class LegacyQualificationQuotaStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A legacy qualification quota. Its code is unique within a tenant. */
data class LegacyQualificationQuota(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var attempts: Int,
    var expiresAt: Int,
    var visible: Boolean,
    var margin: Double,
    var etaMinutes: Int,
    var budget: Double,
    var status: LegacyQualificationQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyQualificationQuotaStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyQualificationQuotaStatus): Boolean {
        when (status) {
            LegacyQualificationQuotaStatus.PROPOSED -> {
                when (next) {
                    LegacyQualificationQuotaStatus.APPROVED, LegacyQualificationQuotaStatus.REJECTED -> return true
                }
                return false
            }
            LegacyQualificationQuotaStatus.APPROVED -> return next == LegacyQualificationQuotaStatus.REJECTED || next == LegacyQualificationQuotaStatus.WITHDRAWN
            LegacyQualificationQuotaStatus.REJECTED -> return next == LegacyQualificationQuotaStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyQualificationQuotaStatus) {
        if (!canMoveTo(next)) {
            error("legacy qualification quota $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun effectiveTotal(): Double = round((margin + attempts.toDouble()) * 100) / 100

    fun snapshot(): LegacyQualificationQuota {
        return LegacyQualificationQuota(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            attempts = attempts,
            expiresAt = expiresAt,
            visible = visible,
            margin = margin,
            etaMinutes = etaMinutes,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy qualification quota in status proposed. */
fun newLegacyQualificationQuota(code: String, title: String): LegacyQualificationQuota {
    return LegacyQualificationQuota(
        code = code,
        title = title,
        fuelLiters = 0.0,
        attempts = 0,
        expiresAt = 0,
        visible = false,
        margin = 0.0,
        etaMinutes = 0,
        budget = 0.0,
        status = LegacyQualificationQuotaStatus.PROPOSED,
    )
}
