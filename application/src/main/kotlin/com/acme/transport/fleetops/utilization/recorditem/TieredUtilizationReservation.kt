package com.acme.transport.fleetops.utilization.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered utilization reservation is in its life cycle. */
enum class TieredUtilizationReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One tiered utilization reservation, with what the planners need to act on it. */
data class TieredUtilizationReservation(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var description: String,
    var stopCount: Int,
    var endsAt: Int,
    var country: String,
    var cost: Double,
    var status: TieredUtilizationReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredUtilizationReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredUtilizationReservationStatus): Boolean {
        when (status) {
            TieredUtilizationReservationStatus.IDLE -> {
                when (next) {
                    TieredUtilizationReservationStatus.WARMING, TieredUtilizationReservationStatus.STOPPED -> return true
                }
                return false
            }
            TieredUtilizationReservationStatus.WARMING -> return next == TieredUtilizationReservationStatus.READY || next == TieredUtilizationReservationStatus.DRAINING
            TieredUtilizationReservationStatus.READY -> {
                when (next) {
                    TieredUtilizationReservationStatus.DRAINING -> return true
                }
                return false
            }
            TieredUtilizationReservationStatus.DRAINING -> {
                when (next) {
                    TieredUtilizationReservationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredUtilizationReservationStatus) {
        if (!canMoveTo(next)) {
            error("tiered utilization reservation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun projectedLoad(): Double = round(stopCount.toDouble() * cost * 100) / 100

    fun snapshot(): TieredUtilizationReservation {
        return TieredUtilizationReservation(
            code = code,
            title = title,
            pinned = pinned,
            description = description,
            stopCount = stopCount,
            endsAt = endsAt,
            country = country,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tiered utilization reservation in status idle. */
fun newTieredUtilizationReservation(code: String, title: String): TieredUtilizationReservation {
    return TieredUtilizationReservation(
        code = code,
        title = title,
        pinned = false,
        description = "",
        stopCount = 0,
        endsAt = 0,
        country = "",
        cost = 0.0,
        status = TieredUtilizationReservationStatus.IDLE,
    )
}
