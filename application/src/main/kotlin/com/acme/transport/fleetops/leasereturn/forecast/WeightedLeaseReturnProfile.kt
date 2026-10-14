package com.acme.transport.fleetops.leasereturn.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted lease return profile is in its life cycle. */
enum class WeightedLeaseReturnProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One weighted lease return profile, with what the planners need to act on it. */
data class WeightedLeaseReturnProfile(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var updatedAt: Int,
    var title: String,
    var vehicleId: String,
    var capacity: Int,
    var region: String,
    var archived: Boolean,
    var source: String,
    var status: WeightedLeaseReturnProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedLeaseReturnProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedLeaseReturnProfileStatus): Boolean {
        when (status) {
            WeightedLeaseReturnProfileStatus.PLANNED -> return next == WeightedLeaseReturnProfileStatus.IN_PROGRESS || next == WeightedLeaseReturnProfileStatus.CANCELLED
            WeightedLeaseReturnProfileStatus.SCHEDULED -> {
                when (next) {
                    WeightedLeaseReturnProfileStatus.COMPLETED -> return true
                }
                return false
            }
            WeightedLeaseReturnProfileStatus.IN_PROGRESS -> return next == WeightedLeaseReturnProfileStatus.COMPLETED || next == WeightedLeaseReturnProfileStatus.CANCELLED
            WeightedLeaseReturnProfileStatus.COMPLETED -> {
                when (next) {
                    WeightedLeaseReturnProfileStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedLeaseReturnProfileStatus) {
        if (!canMoveTo(next)) {
            error("weighted lease return profile $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"weighted lease return profile"} $code"

    fun adjustedValue(): Double = round((capacity.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedLeaseReturnProfile {
        return WeightedLeaseReturnProfile(
            code = code,
            label = label,
            pinned = pinned,
            updatedAt = updatedAt,
            title = title,
            vehicleId = vehicleId,
            capacity = capacity,
            region = region,
            archived = archived,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a weighted lease return profile in status planned. */
fun newWeightedLeaseReturnProfile(code: String, label: String): WeightedLeaseReturnProfile {
    return WeightedLeaseReturnProfile(
        code = code,
        label = label,
        pinned = false,
        updatedAt = 0,
        title = "",
        vehicleId = "",
        capacity = 0,
        region = "",
        archived = false,
        source = "",
        status = WeightedLeaseReturnProfileStatus.PLANNED,
    )
}
