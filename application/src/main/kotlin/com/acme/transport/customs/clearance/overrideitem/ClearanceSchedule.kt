package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance schedule is in its life cycle. */
enum class ClearanceScheduleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A clearance schedule as the clearance team keeps it. */
data class ClearanceSchedule(
    var code: String,
    var name: String,
    var quantity: Int,
    var visible: Boolean,
    var routeCode: String,
    var etaMinutes: Int,
    var margin: Double,
    var score: Double,
    var description: String,
    var currency: String,
    var status: ClearanceScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceScheduleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceScheduleStatus): Boolean {
        when (status) {
            ClearanceScheduleStatus.IDLE -> return next == ClearanceScheduleStatus.DRAINING
            ClearanceScheduleStatus.WARMING -> return next == ClearanceScheduleStatus.DRAINING
            ClearanceScheduleStatus.READY -> {
                when (next) {
                    ClearanceScheduleStatus.DRAINING, ClearanceScheduleStatus.STOPPED -> return true
                }
                return false
            }
            ClearanceScheduleStatus.DRAINING -> {
                when (next) {
                    ClearanceScheduleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceScheduleStatus) {
        if (!canMoveTo(next)) {
            error("clearance schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedValue(): Double = round((quantity.toDouble() - score) * 100) / 100

    fun snapshot(): ClearanceSchedule {
        return ClearanceSchedule(
            code = code,
            name = name,
            quantity = quantity,
            visible = visible,
            routeCode = routeCode,
            etaMinutes = etaMinutes,
            margin = margin,
            score = score,
            description = description,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a clearance schedule in status idle. */
fun newClearanceSchedule(code: String, name: String): ClearanceSchedule {
    return ClearanceSchedule(
        code = code,
        name = name,
        quantity = 0,
        visible = false,
        routeCode = "",
        etaMinutes = 0,
        margin = 0.0,
        score = 0.0,
        description = "",
        currency = "",
        status = ClearanceScheduleStatus.IDLE,
    )
}
