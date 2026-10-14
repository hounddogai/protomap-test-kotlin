package com.acme.transport.customs.broker.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy broker reservation is in its life cycle. */
enum class LegacyBrokerReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy broker reservation as the broker team keeps it. */
data class LegacyBrokerReservation(
    var code: String,
    var label: String,
    var sequence: Int,
    var visible: Boolean,
    var limit: Int,
    var status: LegacyBrokerReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyBrokerReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyBrokerReservationStatus): Boolean {
        when (status) {
            LegacyBrokerReservationStatus.IDLE -> return next == LegacyBrokerReservationStatus.WARMING
            LegacyBrokerReservationStatus.WARMING -> return next == LegacyBrokerReservationStatus.READY || next == LegacyBrokerReservationStatus.DRAINING
            LegacyBrokerReservationStatus.READY -> return next == LegacyBrokerReservationStatus.DRAINING || next == LegacyBrokerReservationStatus.STOPPED
            LegacyBrokerReservationStatus.DRAINING -> {
                when (next) {
                    LegacyBrokerReservationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyBrokerReservationStatus) {
        if (!canMoveTo(next)) {
            error("legacy broker reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveLoad(): Double = round((limit.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): LegacyBrokerReservation {
        return LegacyBrokerReservation(
            code = code,
            label = label,
            sequence = sequence,
            visible = visible,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy broker reservation in status idle. */
fun newLegacyBrokerReservation(code: String, label: String): LegacyBrokerReservation {
    return LegacyBrokerReservation(
        code = code,
        label = label,
        sequence = 0,
        visible = false,
        limit = 0,
        status = LegacyBrokerReservationStatus.IDLE,
    )
}
