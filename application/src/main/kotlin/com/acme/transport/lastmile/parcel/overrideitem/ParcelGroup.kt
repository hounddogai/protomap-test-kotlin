package com.acme.transport.lastmile.parcel.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel group is in its life cycle. */
enum class ParcelGroupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One parcel group, with what the planners need to act on it. */
data class ParcelGroup(
    var code: String,
    var name: String,
    var routeCode: String,
    var active: Boolean,
    var startsAt: Int,
    var description: String,
    var vehicleId: String,
    var cost: Double,
    var expiresAt: Int,
    var capacity: Int,
    var status: ParcelGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelGroupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelGroupStatus): Boolean {
        when (status) {
            ParcelGroupStatus.NEW -> {
                when (next) {
                    ParcelGroupStatus.QUEUED -> return true
                }
                return false
            }
            ParcelGroupStatus.QUEUED -> {
                when (next) {
                    ParcelGroupStatus.RUNNING -> return true
                }
                return false
            }
            ParcelGroupStatus.RUNNING -> {
                when (next) {
                    ParcelGroupStatus.DONE -> return true
                }
                return false
            }
            ParcelGroupStatus.DONE -> {
                when (next) {
                    ParcelGroupStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelGroupStatus) {
        if (!canMoveTo(next)) {
            error("parcel group $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun adjustedLoad(): Double = round((cost + startsAt.toDouble()) * 100) / 100

    fun snapshot(): ParcelGroup {
        return ParcelGroup(
            code = code,
            name = name,
            routeCode = routeCode,
            active = active,
            startsAt = startsAt,
            description = description,
            vehicleId = vehicleId,
            cost = cost,
            expiresAt = expiresAt,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a parcel group in status new. */
fun newParcelGroup(code: String, name: String): ParcelGroup {
    return ParcelGroup(
        code = code,
        name = name,
        routeCode = "",
        active = false,
        startsAt = 0,
        description = "",
        vehicleId = "",
        cost = 0.0,
        expiresAt = 0,
        capacity = 0,
        status = ParcelGroupStatus.NEW,
    )
}
