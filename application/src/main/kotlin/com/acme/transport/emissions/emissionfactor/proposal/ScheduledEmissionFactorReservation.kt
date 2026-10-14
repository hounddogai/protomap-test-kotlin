package com.acme.transport.emissions.emissionfactor.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled emission factor reservation is in its life cycle. */
enum class ScheduledEmissionFactorReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A scheduled emission factor reservation. Its code is unique within a tenant. */
data class ScheduledEmissionFactorReservation(
    var code: String,
    var title: String,
    var version: Int,
    var sequence: Int,
    var locale: String,
    var dock: String,
    var status: ScheduledEmissionFactorReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledEmissionFactorReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledEmissionFactorReservationStatus): Boolean {
        when (status) {
            ScheduledEmissionFactorReservationStatus.IDLE -> {
                when (next) {
                    ScheduledEmissionFactorReservationStatus.WARMING, ScheduledEmissionFactorReservationStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledEmissionFactorReservationStatus.WARMING -> return next == ScheduledEmissionFactorReservationStatus.READY
            ScheduledEmissionFactorReservationStatus.READY -> {
                when (next) {
                    ScheduledEmissionFactorReservationStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledEmissionFactorReservationStatus.DRAINING -> {
                when (next) {
                    ScheduledEmissionFactorReservationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledEmissionFactorReservationStatus) {
        if (!canMoveTo(next)) {
            error("scheduled emission factor reservation $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"scheduled emission factor reservation"} $code"

    fun adjustedVolume(): Double = round((sequence.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): ScheduledEmissionFactorReservation {
        return ScheduledEmissionFactorReservation(
            code = code,
            title = title,
            version = version,
            sequence = sequence,
            locale = locale,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a scheduled emission factor reservation in status idle. */
fun newScheduledEmissionFactorReservation(code: String, title: String): ScheduledEmissionFactorReservation {
    return ScheduledEmissionFactorReservation(
        code = code,
        title = title,
        version = 0,
        sequence = 0,
        locale = "",
        dock = "",
        status = ScheduledEmissionFactorReservationStatus.IDLE,
    )
}
