package com.acme.transport.linehaul.relay.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay reservation is in its life cycle. */
enum class RelayReservationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One relay reservation, with what the planners need to act on it. */
data class RelayReservation(
    var code: String,
    var name: String,
    var channel: String,
    var quantity: Int,
    var unit: String,
    var externalRef: String,
    var active: Boolean,
    var ceiling: Double,
    var checksum: String,
    var dock: String,
    var approved: Boolean,
    var status: RelayReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayReservationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayReservationStatus): Boolean {
        when (status) {
            RelayReservationStatus.PROPOSED -> {
                when (next) {
                    RelayReservationStatus.REJECTED -> return true
                }
                return false
            }
            RelayReservationStatus.APPROVED -> {
                when (next) {
                    RelayReservationStatus.WITHDRAWN -> return true
                }
                return false
            }
            RelayReservationStatus.REJECTED -> return next == RelayReservationStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RelayReservationStatus) {
        if (!canMoveTo(next)) {
            error("relay reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"relay reservation"} $code"

    fun weightedLoad(): Double = round(ceiling * quantity.toDouble() * 100) / 100

    fun snapshot(): RelayReservation {
        return RelayReservation(
            code = code,
            name = name,
            channel = channel,
            quantity = quantity,
            unit = unit,
            externalRef = externalRef,
            active = active,
            ceiling = ceiling,
            checksum = checksum,
            dock = dock,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a relay reservation in status proposed. */
fun newRelayReservation(code: String, name: String): RelayReservation {
    return RelayReservation(
        code = code,
        name = name,
        channel = "",
        quantity = 0,
        unit = "",
        externalRef = "",
        active = false,
        ceiling = 0.0,
        checksum = "",
        dock = "",
        approved = false,
        status = RelayReservationStatus.PROPOSED,
    )
}
