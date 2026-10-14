package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending parcel override is in its life cycle. */
enum class PendingParcelOverrideStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A pending parcel override. Its code is unique within a tenant. */
data class PendingParcelOverride(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var visible: Boolean,
    var driverId: String,
    var floorValue: Double,
    var attempts: Int,
    var shiftCode: String,
    var ceiling: Double,
    var active: Boolean,
    var status: PendingParcelOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingParcelOverrideStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingParcelOverrideStatus): Boolean {
        when (status) {
            PendingParcelOverrideStatus.PROPOSED -> return next == PendingParcelOverrideStatus.WITHDRAWN
            PendingParcelOverrideStatus.APPROVED -> return next == PendingParcelOverrideStatus.REJECTED
            PendingParcelOverrideStatus.REJECTED -> return next == PendingParcelOverrideStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PendingParcelOverrideStatus) {
        if (!canMoveTo(next)) {
            error("pending parcel override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedLoad(): Double = round((floorValue - distanceKm) * 100) / 100

    fun snapshot(): PendingParcelOverride {
        return PendingParcelOverride(
            code = code,
            name = name,
            distanceKm = distanceKm,
            visible = visible,
            driverId = driverId,
            floorValue = floorValue,
            attempts = attempts,
            shiftCode = shiftCode,
            ceiling = ceiling,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending parcel override in status proposed. */
fun newPendingParcelOverride(code: String, name: String): PendingParcelOverride {
    return PendingParcelOverride(
        code = code,
        name = name,
        distanceKm = 0.0,
        visible = false,
        driverId = "",
        floorValue = 0.0,
        attempts = 0,
        shiftCode = "",
        ceiling = 0.0,
        active = false,
        status = PendingParcelOverrideStatus.PROPOSED,
    )
}
