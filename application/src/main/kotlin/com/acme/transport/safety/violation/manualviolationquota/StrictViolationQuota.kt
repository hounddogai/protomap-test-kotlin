package com.acme.transport.safety.violation.manualviolationquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict violation quota records of safety's violation work, and the rules, stores, and calculations
// around them.

/** Where a strict violation quota is in its life cycle. */
enum class StrictViolationQuotaStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A strict violation quota. Its code is unique within a tenant. */
data class StrictViolationQuota(
    var code: String,
    var title: String,
    var graceDays: Int,
    var etaMinutes: Int,
    var margin: Double,
    var weight: Double,
    var expiresAt: Int,
    var windowDays: Int,
    var revision: Int,
    var fuelLiters: Double,
    var label: String,
    var status: StrictViolationQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictViolationQuotaStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictViolationQuotaStatus): Boolean {
        when (status) {
            StrictViolationQuotaStatus.PLANNED -> return next == StrictViolationQuotaStatus.IN_PROGRESS || next == StrictViolationQuotaStatus.CANCELLED
            StrictViolationQuotaStatus.SCHEDULED -> return next == StrictViolationQuotaStatus.IN_PROGRESS
            StrictViolationQuotaStatus.IN_PROGRESS -> {
                when (next) {
                    StrictViolationQuotaStatus.COMPLETED, StrictViolationQuotaStatus.CANCELLED -> return true
                }
                return false
            }
            StrictViolationQuotaStatus.COMPLETED -> return next == StrictViolationQuotaStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: StrictViolationQuotaStatus) {
        if (!canMoveTo(next)) {
            error("strict violation quota $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code [$title]"

    fun weightedLoad(): Double = round((margin + graceDays.toDouble()) * 100) / 100

    fun snapshot(): StrictViolationQuota {
        return StrictViolationQuota(
            code = code,
            title = title,
            graceDays = graceDays,
            etaMinutes = etaMinutes,
            margin = margin,
            weight = weight,
            expiresAt = expiresAt,
            windowDays = windowDays,
            revision = revision,
            fuelLiters = fuelLiters,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict violation quota in status planned. */
fun newStrictViolationQuota(code: String, title: String): StrictViolationQuota {
    return StrictViolationQuota(
        code = code,
        title = title,
        graceDays = 0,
        etaMinutes = 0,
        margin = 0.0,
        weight = 0.0,
        expiresAt = 0,
        windowDays = 0,
        revision = 0,
        fuelLiters = 0.0,
        label = "",
        status = StrictViolationQuotaStatus.PLANNED,
    )
}
