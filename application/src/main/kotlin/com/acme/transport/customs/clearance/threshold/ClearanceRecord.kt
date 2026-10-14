package com.acme.transport.customs.clearance.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance record is in its life cycle. */
enum class ClearanceRecordStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A clearance record. Its code is unique within a tenant. */
data class ClearanceRecord(
    var code: String,
    var title: String,
    var odometer: Int,
    var lane: String,
    var weight: Double,
    var capacity: Int,
    var segment: String,
    var expiresAt: Int,
    var status: ClearanceRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceRecordStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceRecordStatus): Boolean {
        when (status) {
            ClearanceRecordStatus.IDLE -> {
                when (next) {
                    ClearanceRecordStatus.WARMING, ClearanceRecordStatus.DRAINING -> return true
                }
                return false
            }
            ClearanceRecordStatus.WARMING -> return next == ClearanceRecordStatus.READY || next == ClearanceRecordStatus.DRAINING
            ClearanceRecordStatus.READY -> return next == ClearanceRecordStatus.STOPPED
            ClearanceRecordStatus.DRAINING -> {
                when (next) {
                    ClearanceRecordStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceRecordStatus) {
        if (!canMoveTo(next)) {
            error("clearance record $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netExposure(): Double = round((capacity.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): ClearanceRecord {
        return ClearanceRecord(
            code = code,
            title = title,
            odometer = odometer,
            lane = lane,
            weight = weight,
            capacity = capacity,
            segment = segment,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a clearance record in status idle. */
fun newClearanceRecord(code: String, title: String): ClearanceRecord {
    return ClearanceRecord(
        code = code,
        title = title,
        odometer = 0,
        lane = "",
        weight = 0.0,
        capacity = 0,
        segment = "",
        expiresAt = 0,
        status = ClearanceRecordStatus.IDLE,
    )
}
