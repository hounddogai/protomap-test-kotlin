package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop threshold is in its life cycle. */
enum class DropThresholdStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a drop threshold and its current status. */
data class DropThreshold(
    var code: String,
    var title: String,
    var startsAt: Int,
    var approved: Boolean,
    var windowDays: Int,
    var odometer: Int,
    var floorValue: Double,
    var quantity: Int,
    var loadKg: Double,
    var active: Boolean,
    var status: DropThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropThresholdStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropThresholdStatus): Boolean {
        when (status) {
            DropThresholdStatus.IDLE -> return next == DropThresholdStatus.WARMING
            DropThresholdStatus.WARMING -> return next == DropThresholdStatus.READY || next == DropThresholdStatus.STOPPED
            DropThresholdStatus.READY -> {
                when (next) {
                    DropThresholdStatus.DRAINING, DropThresholdStatus.STOPPED -> return true
                }
                return false
            }
            DropThresholdStatus.DRAINING -> {
                when (next) {
                    DropThresholdStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropThresholdStatus) {
        if (!canMoveTo(next)) {
            error("drop threshold $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"drop threshold"} $code"

    fun adjustedVolume(): Double = round(quantity.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): DropThreshold {
        return DropThreshold(
            code = code,
            title = title,
            startsAt = startsAt,
            approved = approved,
            windowDays = windowDays,
            odometer = odometer,
            floorValue = floorValue,
            quantity = quantity,
            loadKg = loadKg,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a drop threshold in status idle. */
fun newDropThreshold(code: String, title: String): DropThreshold {
    return DropThreshold(
        code = code,
        title = title,
        startsAt = 0,
        approved = false,
        windowDays = 0,
        odometer = 0,
        floorValue = 0.0,
        quantity = 0,
        loadKg = 0.0,
        active = false,
        status = DropThresholdStatus.IDLE,
    )
}
