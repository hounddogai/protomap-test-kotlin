package com.acme.transport.network.zone.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered zone reservation is in its life cycle. */
enum class TieredZoneReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One tiered zone reservation, with what the planners need to act on it. */
data class TieredZoneReservation(
    var code: String,
    var title: String,
    var ratio: Double,
    var revision: Int,
    var description: String,
    var fuelLiters: Double,
    var etaMinutes: Int,
    var status: TieredZoneReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredZoneReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredZoneReservationStatus): Boolean {
        when (status) {
            TieredZoneReservationStatus.IDLE -> return next == TieredZoneReservationStatus.WARMING || next == TieredZoneReservationStatus.READY
            TieredZoneReservationStatus.WARMING -> return next == TieredZoneReservationStatus.DRAINING || next == TieredZoneReservationStatus.STOPPED
            TieredZoneReservationStatus.READY -> {
                when (next) {
                    TieredZoneReservationStatus.DRAINING, TieredZoneReservationStatus.STOPPED -> return true
                }
                return false
            }
            TieredZoneReservationStatus.DRAINING -> return next == TieredZoneReservationStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TieredZoneReservationStatus) {
        if (!canMoveTo(next)) {
            error("tiered zone reservation $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"tiered zone reservation"} $code"

    fun weightedTotal(): Double = round((revision.toDouble() + ratio) * 100) / 100

    fun snapshot(): TieredZoneReservation {
        return TieredZoneReservation(
            code = code,
            title = title,
            ratio = ratio,
            revision = revision,
            description = description,
            fuelLiters = fuelLiters,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered zone reservation in status idle. */
fun newTieredZoneReservation(code: String, title: String): TieredZoneReservation {
    return TieredZoneReservation(
        code = code,
        title = title,
        ratio = 0.0,
        revision = 0,
        description = "",
        fuelLiters = 0.0,
        etaMinutes = 0,
        status = TieredZoneReservationStatus.IDLE,
    )
}
