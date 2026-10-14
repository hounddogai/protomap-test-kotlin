package com.acme.transport.terminal.dockdoor.doorthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted dock door exception is in its life cycle. */
enum class WeightedDockDoorExceptionStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A weighted dock door exception. Its code is unique within a tenant. */
data class WeightedDockDoorException(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var locale: String,
    var carrier: String,
    var etaMinutes: Int,
    var driverId: String,
    var attempts: Int,
    var archived: Boolean,
    var stopCount: Int,
    var status: WeightedDockDoorExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDockDoorExceptionStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDockDoorExceptionStatus): Boolean {
        when (status) {
            WeightedDockDoorExceptionStatus.NEW -> return next == WeightedDockDoorExceptionStatus.DONE || next == WeightedDockDoorExceptionStatus.FAILED
            WeightedDockDoorExceptionStatus.QUEUED -> {
                when (next) {
                    WeightedDockDoorExceptionStatus.RUNNING, WeightedDockDoorExceptionStatus.DONE -> return true
                }
                return false
            }
            WeightedDockDoorExceptionStatus.RUNNING -> {
                when (next) {
                    WeightedDockDoorExceptionStatus.DONE -> return true
                }
                return false
            }
            WeightedDockDoorExceptionStatus.DONE -> {
                when (next) {
                    WeightedDockDoorExceptionStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedDockDoorExceptionStatus) {
        if (!canMoveTo(next)) {
            error("weighted dock door exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netVolume(): Double = round((distanceKm - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): WeightedDockDoorException {
        return WeightedDockDoorException(
            code = code,
            title = title,
            distanceKm = distanceKm,
            locale = locale,
            carrier = carrier,
            etaMinutes = etaMinutes,
            driverId = driverId,
            attempts = attempts,
            archived = archived,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a weighted dock door exception in status new. */
fun newWeightedDockDoorException(code: String, title: String): WeightedDockDoorException {
    return WeightedDockDoorException(
        code = code,
        title = title,
        distanceKm = 0.0,
        locale = "",
        carrier = "",
        etaMinutes = 0,
        driverId = "",
        attempts = 0,
        archived = false,
        stopCount = 0,
        status = WeightedDockDoorExceptionStatus.NEW,
    )
}
