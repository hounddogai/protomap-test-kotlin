package com.acme.transport.lastmile.drop.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual drop audit is in its life cycle. */
enum class ManualDropAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a manual drop audit and its current status. */
data class ManualDropAudit(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var loadKg: Double,
    var retries: Int,
    var fuelLiters: Double,
    var name: String,
    var vehicleId: String,
    var status: ManualDropAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDropAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDropAuditStatus): Boolean {
        when (status) {
            ManualDropAuditStatus.PLANNED -> {
                when (next) {
                    ManualDropAuditStatus.IN_PROGRESS, ManualDropAuditStatus.COMPLETED -> return true
                }
                return false
            }
            ManualDropAuditStatus.SCHEDULED -> {
                when (next) {
                    ManualDropAuditStatus.IN_PROGRESS, ManualDropAuditStatus.CANCELLED -> return true
                }
                return false
            }
            ManualDropAuditStatus.IN_PROGRESS -> {
                when (next) {
                    ManualDropAuditStatus.COMPLETED, ManualDropAuditStatus.CANCELLED -> return true
                }
                return false
            }
            ManualDropAuditStatus.COMPLETED -> return next == ManualDropAuditStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ManualDropAuditStatus) {
        if (!canMoveTo(next)) {
            error("manual drop audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedTotal(): Double = round((distanceKm + retries.toDouble()) * 100) / 100

    fun snapshot(): ManualDropAudit {
        return ManualDropAudit(
            code = code,
            title = title,
            distanceKm = distanceKm,
            loadKg = loadKg,
            retries = retries,
            fuelLiters = fuelLiters,
            name = name,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual drop audit in status planned. */
fun newManualDropAudit(code: String, title: String): ManualDropAudit {
    return ManualDropAudit(
        code = code,
        title = title,
        distanceKm = 0.0,
        loadKg = 0.0,
        retries = 0,
        fuelLiters = 0.0,
        name = "",
        vehicleId = "",
        status = ManualDropAuditStatus.PLANNED,
    )
}
