package com.acme.transport.freightpay.dispute.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute record is in its life cycle. */
enum class DisputeRecordStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One dispute record, with what the planners need to act on it. */
data class DisputeRecord(
    var code: String,
    var label: String,
    var attempts: Int,
    var category: String,
    var depot: String,
    var maximum: Double,
    var weight: Double,
    var priority: Int,
    var stopCount: Int,
    var startsAt: Int,
    var driverId: String,
    var status: DisputeRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeRecordStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeRecordStatus): Boolean {
        when (status) {
            DisputeRecordStatus.IDLE -> {
                when (next) {
                    DisputeRecordStatus.WARMING, DisputeRecordStatus.STOPPED -> return true
                }
                return false
            }
            DisputeRecordStatus.WARMING -> {
                when (next) {
                    DisputeRecordStatus.READY -> return true
                }
                return false
            }
            DisputeRecordStatus.READY -> return next == DisputeRecordStatus.STOPPED
            DisputeRecordStatus.DRAINING -> {
                when (next) {
                    DisputeRecordStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeRecordStatus) {
        if (!canMoveTo(next)) {
            error("dispute record $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedTotal(): Double = round(attempts.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): DisputeRecord {
        return DisputeRecord(
            code = code,
            label = label,
            attempts = attempts,
            category = category,
            depot = depot,
            maximum = maximum,
            weight = weight,
            priority = priority,
            stopCount = stopCount,
            startsAt = startsAt,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a dispute record in status idle. */
fun newDisputeRecord(code: String, label: String): DisputeRecord {
    return DisputeRecord(
        code = code,
        label = label,
        attempts = 0,
        category = "",
        depot = "",
        maximum = 0.0,
        weight = 0.0,
        priority = 0,
        stopCount = 0,
        startsAt = 0,
        driverId = "",
        status = DisputeRecordStatus.IDLE,
    )
}
