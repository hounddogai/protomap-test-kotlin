package com.acme.transport.customs.inspection.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict inspection profile is in its life cycle. */
enum class StrictInspectionProfileStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A strict inspection profile. Its code is unique within a tenant. */
data class StrictInspectionProfile(
    var code: String,
    var name: String,
    var vehicleId: String,
    var createdAt: Int,
    var dock: String,
    var cost: Double,
    var ownerId: String,
    var maximum: Double,
    var label: String,
    var contactEmail: String,
    var status: StrictInspectionProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictInspectionProfileStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictInspectionProfileStatus): Boolean {
        when (status) {
            StrictInspectionProfileStatus.REQUESTED -> return next == StrictInspectionProfileStatus.GRANTED
            StrictInspectionProfileStatus.GRANTED -> {
                when (next) {
                    StrictInspectionProfileStatus.REVOKED, StrictInspectionProfileStatus.EXPIRED -> return true
                }
                return false
            }
            StrictInspectionProfileStatus.REVOKED -> return next == StrictInspectionProfileStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: StrictInspectionProfileStatus) {
        if (!canMoveTo(next)) {
            error("strict inspection profile $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($name)"

    fun adjustedVolume(): Double = round((createdAt.toDouble() - maximum) * 100) / 100

    fun snapshot(): StrictInspectionProfile {
        return StrictInspectionProfile(
            code = code,
            name = name,
            vehicleId = vehicleId,
            createdAt = createdAt,
            dock = dock,
            cost = cost,
            ownerId = ownerId,
            maximum = maximum,
            label = label,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict inspection profile in status requested. */
fun newStrictInspectionProfile(code: String, name: String): StrictInspectionProfile {
    return StrictInspectionProfile(
        code = code,
        name = name,
        vehicleId = "",
        createdAt = 0,
        dock = "",
        cost = 0.0,
        ownerId = "",
        maximum = 0.0,
        label = "",
        contactEmail = "",
        status = StrictInspectionProfileStatus.REQUESTED,
    )
}
