package com.acme.transport.customs.clearance.clearancemilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance quota is in its life cycle. */
enum class ClearanceQuotaStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A clearance quota as the clearance team keeps it. */
data class ClearanceQuota(
    var code: String,
    var title: String,
    var notes: String,
    var durationMinutes: Int,
    var pallets: Int,
    var description: String,
    var endsAt: Int,
    var rate: Double,
    var odometer: Int,
    var status: ClearanceQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceQuotaStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceQuotaStatus): Boolean {
        when (status) {
            ClearanceQuotaStatus.PLANNED -> return next == ClearanceQuotaStatus.IN_PROGRESS
            ClearanceQuotaStatus.SCHEDULED -> {
                when (next) {
                    ClearanceQuotaStatus.COMPLETED, ClearanceQuotaStatus.CANCELLED -> return true
                }
                return false
            }
            ClearanceQuotaStatus.IN_PROGRESS -> {
                when (next) {
                    ClearanceQuotaStatus.COMPLETED, ClearanceQuotaStatus.CANCELLED -> return true
                }
                return false
            }
            ClearanceQuotaStatus.COMPLETED -> return next == ClearanceQuotaStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ClearanceQuotaStatus) {
        if (!canMoveTo(next)) {
            error("clearance quota $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun effectiveLoad(): Double = round(odometer.toDouble() * rate * 100) / 100

    fun snapshot(): ClearanceQuota {
        return ClearanceQuota(
            code = code,
            title = title,
            notes = notes,
            durationMinutes = durationMinutes,
            pallets = pallets,
            description = description,
            endsAt = endsAt,
            rate = rate,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a clearance quota in status planned. */
fun newClearanceQuota(code: String, title: String): ClearanceQuota {
    return ClearanceQuota(
        code = code,
        title = title,
        notes = "",
        durationMinutes = 0,
        pallets = 0,
        description = "",
        endsAt = 0,
        rate = 0.0,
        odometer = 0,
        status = ClearanceQuotaStatus.PLANNED,
    )
}
