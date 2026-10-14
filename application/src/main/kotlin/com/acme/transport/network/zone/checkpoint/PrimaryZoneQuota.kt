package com.acme.transport.network.zone.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary zone quota is in its life cycle. */
enum class PrimaryZoneQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One primary zone quota, with what the planners need to act on it. */
data class PrimaryZoneQuota(
    var code: String,
    var title: String,
    var name: String,
    var minimum: Double,
    var pallets: Int,
    var limit: Int,
    var createdAt: Int,
    var updatedAt: Int,
    var vehicleId: String,
    var driverId: String,
    var status: PrimaryZoneQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryZoneQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryZoneQuotaStatus): Boolean {
        when (status) {
            PrimaryZoneQuotaStatus.NEW -> {
                when (next) {
                    PrimaryZoneQuotaStatus.QUEUED -> return true
                }
                return false
            }
            PrimaryZoneQuotaStatus.QUEUED -> {
                when (next) {
                    PrimaryZoneQuotaStatus.RUNNING, PrimaryZoneQuotaStatus.FAILED -> return true
                }
                return false
            }
            PrimaryZoneQuotaStatus.RUNNING -> return next == PrimaryZoneQuotaStatus.FAILED
            PrimaryZoneQuotaStatus.DONE -> {
                when (next) {
                    PrimaryZoneQuotaStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryZoneQuotaStatus) {
        if (!canMoveTo(next)) {
            error("primary zone quota $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun adjustedTotal(): Double = round(pallets.toDouble() * minimum * 100) / 100

    fun snapshot(): PrimaryZoneQuota {
        return PrimaryZoneQuota(
            code = code,
            title = title,
            name = name,
            minimum = minimum,
            pallets = pallets,
            limit = limit,
            createdAt = createdAt,
            updatedAt = updatedAt,
            vehicleId = vehicleId,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary zone quota in status new. */
fun newPrimaryZoneQuota(code: String, title: String): PrimaryZoneQuota {
    return PrimaryZoneQuota(
        code = code,
        title = title,
        name = "",
        minimum = 0.0,
        pallets = 0,
        limit = 0,
        createdAt = 0,
        updatedAt = 0,
        vehicleId = "",
        driverId = "",
        status = PrimaryZoneQuotaStatus.NEW,
    )
}
