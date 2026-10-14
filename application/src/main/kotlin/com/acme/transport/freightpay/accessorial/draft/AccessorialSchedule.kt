package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial schedule is in its life cycle. */
enum class AccessorialScheduleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A accessorial schedule. Its code is unique within a tenant. */
data class AccessorialSchedule(
    var code: String,
    var label: String,
    var floorValue: Double,
    var createdAt: Int,
    var visible: Boolean,
    var lane: String,
    var name: String,
    var summary: String,
    var status: AccessorialScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialScheduleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialScheduleStatus): Boolean {
        when (status) {
            AccessorialScheduleStatus.PROPOSED -> return next == AccessorialScheduleStatus.APPROVED || next == AccessorialScheduleStatus.WITHDRAWN
            AccessorialScheduleStatus.APPROVED -> {
                when (next) {
                    AccessorialScheduleStatus.REJECTED -> return true
                }
                return false
            }
            AccessorialScheduleStatus.REJECTED -> {
                when (next) {
                    AccessorialScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialScheduleStatus) {
        if (!canMoveTo(next)) {
            error("accessorial schedule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedTotal(): Double = round((createdAt.toDouble() - floorValue) * 100) / 100

    fun snapshot(): AccessorialSchedule {
        return AccessorialSchedule(
            code = code,
            label = label,
            floorValue = floorValue,
            createdAt = createdAt,
            visible = visible,
            lane = lane,
            name = name,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a accessorial schedule in status proposed. */
fun newAccessorialSchedule(code: String, label: String): AccessorialSchedule {
    return AccessorialSchedule(
        code = code,
        label = label,
        floorValue = 0.0,
        createdAt = 0,
        visible = false,
        lane = "",
        name = "",
        summary = "",
        status = AccessorialScheduleStatus.PROPOSED,
    )
}
