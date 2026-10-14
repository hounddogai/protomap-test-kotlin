package com.acme.transport.network.zone.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched zone schedule is in its life cycle. */
enum class BatchedZoneScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A batched zone schedule. Its code is unique within a tenant. */
data class BatchedZoneSchedule(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var carrier: String,
    var version: Int,
    var driverId: String,
    var active: Boolean,
    var visible: Boolean,
    var unit: String,
    var status: BatchedZoneScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedZoneScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedZoneScheduleStatus): Boolean {
        when (status) {
            BatchedZoneScheduleStatus.OPEN -> {
                when (next) {
                    BatchedZoneScheduleStatus.IN_REVIEW, BatchedZoneScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedZoneScheduleStatus.IN_REVIEW -> {
                when (next) {
                    BatchedZoneScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedZoneScheduleStatus.RESOLVED -> {
                when (next) {
                    BatchedZoneScheduleStatus.REOPENED, BatchedZoneScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedZoneScheduleStatus.REOPENED -> {
                when (next) {
                    BatchedZoneScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedZoneScheduleStatus) {
        if (!canMoveTo(next)) {
            error("batched zone schedule $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun projectedVolume(): Double = round((version.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): BatchedZoneSchedule {
        return BatchedZoneSchedule(
            code = code,
            title = title,
            updatedAt = updatedAt,
            carrier = carrier,
            version = version,
            driverId = driverId,
            active = active,
            visible = visible,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a batched zone schedule in status open. */
fun newBatchedZoneSchedule(code: String, title: String): BatchedZoneSchedule {
    return BatchedZoneSchedule(
        code = code,
        title = title,
        updatedAt = 0,
        carrier = "",
        version = 0,
        driverId = "",
        active = false,
        visible = false,
        unit = "",
        status = BatchedZoneScheduleStatus.OPEN,
    )
}
