package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global parcel assignment is in its life cycle. */
enum class GlobalParcelAssignmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global parcel assignment as the parcel team keeps it. */
data class GlobalParcelAssignment(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var loadKg: Double,
    var endsAt: Int,
    var currency: String,
    var ceiling: Double,
    var updatedAt: Int,
    var status: GlobalParcelAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalParcelAssignmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalParcelAssignmentStatus): Boolean {
        when (status) {
            GlobalParcelAssignmentStatus.REQUESTED -> {
                when (next) {
                    GlobalParcelAssignmentStatus.GRANTED -> return true
                }
                return false
            }
            GlobalParcelAssignmentStatus.GRANTED -> {
                when (next) {
                    GlobalParcelAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalParcelAssignmentStatus.REVOKED -> {
                when (next) {
                    GlobalParcelAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalParcelAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("global parcel assignment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun projectedValue(): Double = round((updatedAt.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): GlobalParcelAssignment {
        return GlobalParcelAssignment(
            code = code,
            name = name,
            distanceKm = distanceKm,
            loadKg = loadKg,
            endsAt = endsAt,
            currency = currency,
            ceiling = ceiling,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global parcel assignment in status requested. */
fun newGlobalParcelAssignment(code: String, name: String): GlobalParcelAssignment {
    return GlobalParcelAssignment(
        code = code,
        name = name,
        distanceKm = 0.0,
        loadKg = 0.0,
        endsAt = 0,
        currency = "",
        ceiling = 0.0,
        updatedAt = 0,
        status = GlobalParcelAssignmentStatus.REQUESTED,
    )
}
