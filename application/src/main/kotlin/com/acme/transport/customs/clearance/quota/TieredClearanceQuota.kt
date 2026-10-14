package com.acme.transport.customs.clearance.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered clearance quota records of customs's clearance work, and the rules, stores, and calculations
// around them.

/** Where a tiered clearance quota is in its life cycle. */
enum class TieredClearanceQuotaStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered clearance quota. Its code is unique within a tenant. */
data class TieredClearanceQuota(
    var code: String,
    var name: String,
    var driverId: String,
    var contactEmail: String,
    var updatedAt: Int,
    var vehicleId: String,
    var trailerId: String,
    var budget: Double,
    var sequence: Int,
    var status: TieredClearanceQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredClearanceQuotaStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredClearanceQuotaStatus): Boolean {
        when (status) {
            TieredClearanceQuotaStatus.OPEN -> {
                when (next) {
                    TieredClearanceQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredClearanceQuotaStatus.IN_REVIEW -> {
                when (next) {
                    TieredClearanceQuotaStatus.REOPENED -> return true
                }
                return false
            }
            TieredClearanceQuotaStatus.RESOLVED -> {
                when (next) {
                    TieredClearanceQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredClearanceQuotaStatus.REOPENED -> return next == TieredClearanceQuotaStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredClearanceQuotaStatus) {
        if (!canMoveTo(next)) {
            error("tiered clearance quota $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tiered clearance quota"} $code"

    fun projectedVolume(): Double = round((updatedAt.toDouble() - budget) * 100) / 100

    fun snapshot(): TieredClearanceQuota {
        return TieredClearanceQuota(
            code = code,
            name = name,
            driverId = driverId,
            contactEmail = contactEmail,
            updatedAt = updatedAt,
            vehicleId = vehicleId,
            trailerId = trailerId,
            budget = budget,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered clearance quota in status open. */
fun newTieredClearanceQuota(code: String, name: String): TieredClearanceQuota {
    return TieredClearanceQuota(
        code = code,
        name = name,
        driverId = "",
        contactEmail = "",
        updatedAt = 0,
        vehicleId = "",
        trailerId = "",
        budget = 0.0,
        sequence = 0,
        status = TieredClearanceQuotaStatus.OPEN,
    )
}
