package com.acme.transport.lastmile.drop.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The drop reservation records of lastmile's drop work, and the rules, stores, and calculations around them.

/** Where a drop reservation is in its life cycle. */
enum class DropReservationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A drop reservation. Its code is unique within a tenant. */
data class DropReservation(
    var code: String,
    var label: String,
    var priority: Int,
    var margin: Double,
    var name: String,
    var loadKg: Double,
    var status: DropReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropReservationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DropReservationStatus): Boolean {
        when (status) {
            DropReservationStatus.PROPOSED -> return next == DropReservationStatus.WITHDRAWN
            DropReservationStatus.APPROVED -> return next == DropReservationStatus.REJECTED || next == DropReservationStatus.WITHDRAWN
            DropReservationStatus.REJECTED -> return next == DropReservationStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DropReservationStatus) {
        if (!canMoveTo(next)) {
            error("drop reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"drop reservation"} $code"

    fun netLoad(): Double = round(margin * priority.toDouble() * 100) / 100

    fun snapshot(): DropReservation {
        return DropReservation(
            code = code,
            label = label,
            priority = priority,
            margin = margin,
            name = name,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a drop reservation in status proposed. */
fun newDropReservation(code: String, label: String): DropReservation {
    return DropReservation(
        code = code,
        label = label,
        priority = 0,
        margin = 0.0,
        name = "",
        loadKg = 0.0,
        status = DropReservationStatus.PROPOSED,
    )
}
