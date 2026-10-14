package com.acme.transport.customs.clearance.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled clearance notice is in its life cycle. */
enum class ScheduledClearanceNoticeStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a scheduled clearance notice and its current status. */
data class ScheduledClearanceNotice(
    var code: String,
    var name: String,
    var endsAt: Int,
    var depot: String,
    var loadKg: Double,
    var etaMinutes: Int,
    var shiftCode: String,
    var status: ScheduledClearanceNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledClearanceNoticeStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledClearanceNoticeStatus): Boolean {
        when (status) {
            ScheduledClearanceNoticeStatus.NEW -> {
                when (next) {
                    ScheduledClearanceNoticeStatus.FAILED -> return true
                }
                return false
            }
            ScheduledClearanceNoticeStatus.QUEUED -> {
                when (next) {
                    ScheduledClearanceNoticeStatus.RUNNING, ScheduledClearanceNoticeStatus.FAILED -> return true
                }
                return false
            }
            ScheduledClearanceNoticeStatus.RUNNING -> return next == ScheduledClearanceNoticeStatus.DONE || next == ScheduledClearanceNoticeStatus.FAILED
            ScheduledClearanceNoticeStatus.DONE -> return next == ScheduledClearanceNoticeStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledClearanceNoticeStatus) {
        if (!canMoveTo(next)) {
            error("scheduled clearance notice $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun projectedTotal(): Double = round(loadKg * endsAt.toDouble() * 100) / 100

    fun snapshot(): ScheduledClearanceNotice {
        return ScheduledClearanceNotice(
            code = code,
            name = name,
            endsAt = endsAt,
            depot = depot,
            loadKg = loadKg,
            etaMinutes = etaMinutes,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scheduled clearance notice in status new. */
fun newScheduledClearanceNotice(code: String, name: String): ScheduledClearanceNotice {
    return ScheduledClearanceNotice(
        code = code,
        name = name,
        endsAt = 0,
        depot = "",
        loadKg = 0.0,
        etaMinutes = 0,
        shiftCode = "",
        status = ScheduledClearanceNoticeStatus.NEW,
    )
}
