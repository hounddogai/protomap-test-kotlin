package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel window is in its life cycle. */
enum class ParcelWindowStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One parcel window, with what the planners need to act on it. */
data class ParcelWindow(
    var code: String,
    var name: String,
    var endsAt: Int,
    var updatedAt: Int,
    var rate: Double,
    var distanceKm: Double,
    var vehicleId: String,
    var driverId: String,
    var segment: String,
    var visible: Boolean,
    var timezone: String,
    var status: ParcelWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelWindowStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelWindowStatus): Boolean {
        when (status) {
            ParcelWindowStatus.REQUESTED -> {
                when (next) {
                    ParcelWindowStatus.REVOKED, ParcelWindowStatus.EXPIRED -> return true
                }
                return false
            }
            ParcelWindowStatus.GRANTED -> return next == ParcelWindowStatus.EXPIRED
            ParcelWindowStatus.REVOKED -> {
                when (next) {
                    ParcelWindowStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelWindowStatus) {
        if (!canMoveTo(next)) {
            error("parcel window $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun netExposure(): Double = round((distanceKm + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): ParcelWindow {
        return ParcelWindow(
            code = code,
            name = name,
            endsAt = endsAt,
            updatedAt = updatedAt,
            rate = rate,
            distanceKm = distanceKm,
            vehicleId = vehicleId,
            driverId = driverId,
            segment = segment,
            visible = visible,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a parcel window in status requested. */
fun newParcelWindow(code: String, name: String): ParcelWindow {
    return ParcelWindow(
        code = code,
        name = name,
        endsAt = 0,
        updatedAt = 0,
        rate = 0.0,
        distanceKm = 0.0,
        vehicleId = "",
        driverId = "",
        segment = "",
        visible = false,
        timezone = "",
        status = ParcelWindowStatus.REQUESTED,
    )
}
