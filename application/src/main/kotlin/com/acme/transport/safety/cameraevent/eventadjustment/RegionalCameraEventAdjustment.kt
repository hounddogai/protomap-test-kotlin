package com.acme.transport.safety.cameraevent.eventadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional camera event adjustment records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a regional camera event adjustment is in its life cycle. */
enum class RegionalCameraEventAdjustmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A regional camera event adjustment as the camera event team keeps it. */
data class RegionalCameraEventAdjustment(
    var code: String,
    var name: String,
    var loadKg: Double,
    var stopCount: Int,
    var pinned: Boolean,
    var amount: Double,
    var dock: String,
    var floorValue: Double,
    var status: RegionalCameraEventAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCameraEventAdjustmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCameraEventAdjustmentStatus): Boolean {
        when (status) {
            RegionalCameraEventAdjustmentStatus.IDLE -> return next == RegionalCameraEventAdjustmentStatus.WARMING
            RegionalCameraEventAdjustmentStatus.WARMING -> {
                when (next) {
                    RegionalCameraEventAdjustmentStatus.READY, RegionalCameraEventAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
            RegionalCameraEventAdjustmentStatus.READY -> {
                when (next) {
                    RegionalCameraEventAdjustmentStatus.DRAINING -> return true
                }
                return false
            }
            RegionalCameraEventAdjustmentStatus.DRAINING -> {
                when (next) {
                    RegionalCameraEventAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCameraEventAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("regional camera event adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional camera event adjustment"} $code"

    fun projectedValue(): Double = round((amount - loadKg) * 100) / 100

    fun snapshot(): RegionalCameraEventAdjustment {
        return RegionalCameraEventAdjustment(
            code = code,
            name = name,
            loadKg = loadKg,
            stopCount = stopCount,
            pinned = pinned,
            amount = amount,
            dock = dock,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional camera event adjustment in status idle. */
fun newRegionalCameraEventAdjustment(code: String, name: String): RegionalCameraEventAdjustment {
    return RegionalCameraEventAdjustment(
        code = code,
        name = name,
        loadKg = 0.0,
        stopCount = 0,
        pinned = false,
        amount = 0.0,
        dock = "",
        floorValue = 0.0,
        status = RegionalCameraEventAdjustmentStatus.IDLE,
    )
}
