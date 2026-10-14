package com.acme.transport.fleetops.utilization.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization quota is in its life cycle. */
enum class UtilizationQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A utilization quota as the utilization team keeps it. */
data class UtilizationQuota(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var loadKg: Double,
    var threshold: Double,
    var phone: String,
    var fuelLiters: Double,
    var status: UtilizationQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationQuotaStatus): Boolean {
        when (status) {
            UtilizationQuotaStatus.DRAFT -> return next == UtilizationQuotaStatus.ACTIVE
            UtilizationQuotaStatus.PENDING -> {
                when (next) {
                    UtilizationQuotaStatus.ACTIVE, UtilizationQuotaStatus.CLOSED -> return true
                }
                return false
            }
            UtilizationQuotaStatus.ACTIVE -> return next == UtilizationQuotaStatus.SUSPENDED || next == UtilizationQuotaStatus.CLOSED
            UtilizationQuotaStatus.SUSPENDED -> return next == UtilizationQuotaStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: UtilizationQuotaStatus) {
        if (!canMoveTo(next)) {
            error("utilization quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"utilization quota"} $code"

    fun effectiveExposure(): Double = round((loadKg - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): UtilizationQuota {
        return UtilizationQuota(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            loadKg = loadKg,
            threshold = threshold,
            phone = phone,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a utilization quota in status draft. */
fun newUtilizationQuota(code: String, label: String): UtilizationQuota {
    return UtilizationQuota(
        code = code,
        label = label,
        durationMinutes = 0,
        loadKg = 0.0,
        threshold = 0.0,
        phone = "",
        fuelLiters = 0.0,
        status = UtilizationQuotaStatus.DRAFT,
    )
}
