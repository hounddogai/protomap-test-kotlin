package com.acme.transport.safety.incident.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global incident request is in its life cycle. */
enum class GlobalIncidentRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a global incident request and its current status. */
data class GlobalIncidentRequest(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var createdAt: Int,
    var fuelLiters: Double,
    var ownerId: String,
    var odometer: Int,
    var driverId: String,
    var loadKg: Double,
    var status: GlobalIncidentRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalIncidentRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalIncidentRequestStatus): Boolean {
        when (status) {
            GlobalIncidentRequestStatus.REQUESTED -> {
                when (next) {
                    GlobalIncidentRequestStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalIncidentRequestStatus.GRANTED -> {
                when (next) {
                    GlobalIncidentRequestStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalIncidentRequestStatus.REVOKED -> {
                when (next) {
                    GlobalIncidentRequestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalIncidentRequestStatus) {
        if (!canMoveTo(next)) {
            error("global incident request $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun adjustedLoad(): Double = round((etaMinutes.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): GlobalIncidentRequest {
        return GlobalIncidentRequest(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            createdAt = createdAt,
            fuelLiters = fuelLiters,
            ownerId = ownerId,
            odometer = odometer,
            driverId = driverId,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global incident request in status requested. */
fun newGlobalIncidentRequest(code: String, title: String): GlobalIncidentRequest {
    return GlobalIncidentRequest(
        code = code,
        title = title,
        etaMinutes = 0,
        createdAt = 0,
        fuelLiters = 0.0,
        ownerId = "",
        odometer = 0,
        driverId = "",
        loadKg = 0.0,
        status = GlobalIncidentRequestStatus.REQUESTED,
    )
}
