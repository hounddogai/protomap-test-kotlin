package com.acme.transport.customs.broker.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary broker reservation is in its life cycle. */
enum class PrimaryBrokerReservationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A primary broker reservation. Its code is unique within a tenant. */
data class PrimaryBrokerReservation(
    var code: String,
    var label: String,
    var trailerId: String,
    var endsAt: Int,
    var updatedAt: Int,
    var graceDays: Int,
    var distanceKm: Double,
    var status: PrimaryBrokerReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryBrokerReservationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryBrokerReservationStatus): Boolean {
        when (status) {
            PrimaryBrokerReservationStatus.NEW -> {
                when (next) {
                    PrimaryBrokerReservationStatus.RUNNING, PrimaryBrokerReservationStatus.FAILED -> return true
                }
                return false
            }
            PrimaryBrokerReservationStatus.QUEUED -> return next == PrimaryBrokerReservationStatus.DONE || next == PrimaryBrokerReservationStatus.FAILED
            PrimaryBrokerReservationStatus.RUNNING -> return next == PrimaryBrokerReservationStatus.DONE || next == PrimaryBrokerReservationStatus.FAILED
            PrimaryBrokerReservationStatus.DONE -> return next == PrimaryBrokerReservationStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PrimaryBrokerReservationStatus) {
        if (!canMoveTo(next)) {
            error("primary broker reservation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedVolume(): Double = round(endsAt.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): PrimaryBrokerReservation {
        return PrimaryBrokerReservation(
            code = code,
            label = label,
            trailerId = trailerId,
            endsAt = endsAt,
            updatedAt = updatedAt,
            graceDays = graceDays,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary broker reservation in status new. */
fun newPrimaryBrokerReservation(code: String, label: String): PrimaryBrokerReservation {
    return PrimaryBrokerReservation(
        code = code,
        label = label,
        trailerId = "",
        endsAt = 0,
        updatedAt = 0,
        graceDays = 0,
        distanceKm = 0.0,
        status = PrimaryBrokerReservationStatus.NEW,
    )
}
