package com.acme.transport.fleetops.leasereturn.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return mapping is in its life cycle. */
enum class LeaseReturnMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One lease return mapping, with what the planners need to act on it. */
data class LeaseReturnMapping(
    var code: String,
    var label: String,
    var country: String,
    var driverId: String,
    var channel: String,
    var floorValue: Double,
    var version: Int,
    var segment: String,
    var expiresAt: Int,
    var status: LeaseReturnMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnMappingStatus): Boolean {
        when (status) {
            LeaseReturnMappingStatus.PLANNED -> {
                when (next) {
                    LeaseReturnMappingStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LeaseReturnMappingStatus.SCHEDULED -> {
                when (next) {
                    LeaseReturnMappingStatus.COMPLETED, LeaseReturnMappingStatus.CANCELLED -> return true
                }
                return false
            }
            LeaseReturnMappingStatus.IN_PROGRESS -> {
                when (next) {
                    LeaseReturnMappingStatus.CANCELLED -> return true
                }
                return false
            }
            LeaseReturnMappingStatus.COMPLETED -> return next == LeaseReturnMappingStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LeaseReturnMappingStatus) {
        if (!canMoveTo(next)) {
            error("lease return mapping $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"lease return mapping"} $code"

    fun projectedExposure(): Double = round((expiresAt.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): LeaseReturnMapping {
        return LeaseReturnMapping(
            code = code,
            label = label,
            country = country,
            driverId = driverId,
            channel = channel,
            floorValue = floorValue,
            version = version,
            segment = segment,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lease return mapping in status planned. */
fun newLeaseReturnMapping(code: String, label: String): LeaseReturnMapping {
    return LeaseReturnMapping(
        code = code,
        label = label,
        country = "",
        driverId = "",
        channel = "",
        floorValue = 0.0,
        version = 0,
        segment = "",
        expiresAt = 0,
        status = LeaseReturnMappingStatus.PLANNED,
    )
}
