package com.acme.transport.safety.nearmiss.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped near miss reservation is in its life cycle. */
enum class CappedNearMissReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A capped near miss reservation. Its code is unique within a tenant. */
data class CappedNearMissReservation(
    var code: String,
    var label: String,
    var locale: String,
    var visible: Boolean,
    var cost: Double,
    var version: Int,
    var attributes: MutableMap<String, String>,
    var discount: Double,
    var attempts: Int,
    var vehicleId: String,
    var status: CappedNearMissReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedNearMissReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedNearMissReservationStatus): Boolean {
        when (status) {
            CappedNearMissReservationStatus.PLANNED -> {
                when (next) {
                    CappedNearMissReservationStatus.SCHEDULED, CappedNearMissReservationStatus.CANCELLED -> return true
                }
                return false
            }
            CappedNearMissReservationStatus.SCHEDULED -> return next == CappedNearMissReservationStatus.IN_PROGRESS
            CappedNearMissReservationStatus.IN_PROGRESS -> return next == CappedNearMissReservationStatus.COMPLETED || next == CappedNearMissReservationStatus.CANCELLED
            CappedNearMissReservationStatus.COMPLETED -> {
                when (next) {
                    CappedNearMissReservationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedNearMissReservationStatus) {
        if (!canMoveTo(next)) {
            error("capped near miss reservation $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round((cost - discount) * 100) / 100

    fun snapshot(): CappedNearMissReservation {
        return CappedNearMissReservation(
            code = code,
            label = label,
            locale = locale,
            visible = visible,
            cost = cost,
            version = version,
            attributes = attributes,
            discount = discount,
            attempts = attempts,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a capped near miss reservation in status planned. */
fun newCappedNearMissReservation(code: String, label: String): CappedNearMissReservation {
    return CappedNearMissReservation(
        code = code,
        label = label,
        locale = "",
        visible = false,
        cost = 0.0,
        version = 0,
        attributes = mutableMapOf<String, String>(),
        discount = 0.0,
        attempts = 0,
        vehicleId = "",
        status = CappedNearMissReservationStatus.PLANNED,
    )
}
