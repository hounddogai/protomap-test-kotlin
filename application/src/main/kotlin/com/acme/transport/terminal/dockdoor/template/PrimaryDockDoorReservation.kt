package com.acme.transport.terminal.dockdoor.template

import com.acme.transport.kit.text.normalize

/** Where a primary dock door reservation is in its life cycle. */
enum class PrimaryDockDoorReservationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a primary dock door reservation and its current status. */
data class PrimaryDockDoorReservation(
    var code: String,
    var label: String,
    var batchSize: Int,
    var segment: String,
    var currency: String,
    var status: PrimaryDockDoorReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDockDoorReservationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDockDoorReservationStatus): Boolean {
        when (status) {
            PrimaryDockDoorReservationStatus.REQUESTED -> {
                when (next) {
                    PrimaryDockDoorReservationStatus.GRANTED -> return true
                }
                return false
            }
            PrimaryDockDoorReservationStatus.GRANTED -> {
                when (next) {
                    PrimaryDockDoorReservationStatus.EXPIRED -> return true
                }
                return false
            }
            PrimaryDockDoorReservationStatus.REVOKED -> return next == PrimaryDockDoorReservationStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PrimaryDockDoorReservationStatus) {
        if (!canMoveTo(next)) {
            error("primary dock door reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): PrimaryDockDoorReservation {
        return PrimaryDockDoorReservation(
            code = code,
            label = label,
            batchSize = batchSize,
            segment = segment,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a primary dock door reservation in status requested. */
fun newPrimaryDockDoorReservation(code: String, label: String): PrimaryDockDoorReservation {
    return PrimaryDockDoorReservation(
        code = code,
        label = label,
        batchSize = 0,
        segment = "",
        currency = "",
        status = PrimaryDockDoorReservationStatus.REQUESTED,
    )
}
