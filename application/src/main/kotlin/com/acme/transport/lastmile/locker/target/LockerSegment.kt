package com.acme.transport.lastmile.locker.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker segment is in its life cycle. */
enum class LockerSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A locker segment. Its code is unique within a tenant. */
data class LockerSegment(
    var code: String,
    var label: String,
    var carrier: String,
    var quantity: Int,
    var vehicleId: String,
    var segment: String,
    var capacity: Int,
    var durationMinutes: Int,
    var status: LockerSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerSegmentStatus): Boolean {
        when (status) {
            LockerSegmentStatus.IDLE -> return next == LockerSegmentStatus.WARMING
            LockerSegmentStatus.WARMING -> {
                when (next) {
                    LockerSegmentStatus.READY, LockerSegmentStatus.STOPPED -> return true
                }
                return false
            }
            LockerSegmentStatus.READY -> {
                when (next) {
                    LockerSegmentStatus.DRAINING, LockerSegmentStatus.STOPPED -> return true
                }
                return false
            }
            LockerSegmentStatus.DRAINING -> return next == LockerSegmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LockerSegmentStatus) {
        if (!canMoveTo(next)) {
            error("locker segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netTotal(): Double = round((capacity.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): LockerSegment {
        return LockerSegment(
            code = code,
            label = label,
            carrier = carrier,
            quantity = quantity,
            vehicleId = vehicleId,
            segment = segment,
            capacity = capacity,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a locker segment in status idle. */
fun newLockerSegment(code: String, label: String): LockerSegment {
    return LockerSegment(
        code = code,
        label = label,
        carrier = "",
        quantity = 0,
        vehicleId = "",
        segment = "",
        capacity = 0,
        durationMinutes = 0,
        status = LockerSegmentStatus.IDLE,
    )
}
