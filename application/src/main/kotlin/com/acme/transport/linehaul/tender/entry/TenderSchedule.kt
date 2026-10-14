package com.acme.transport.linehaul.tender.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender schedule is in its life cycle. */
enum class TenderScheduleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tender schedule. Its code is unique within a tenant. */
data class TenderSchedule(
    var code: String,
    var label: String,
    var minimum: Double,
    var stopCount: Int,
    var pinned: Boolean,
    var expiresAt: Int,
    var shiftCode: String,
    var cost: Double,
    var ceiling: Double,
    var score: Double,
    var status: TenderScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderScheduleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderScheduleStatus): Boolean {
        when (status) {
            TenderScheduleStatus.PROPOSED -> return next == TenderScheduleStatus.APPROVED || next == TenderScheduleStatus.WITHDRAWN
            TenderScheduleStatus.APPROVED -> return next == TenderScheduleStatus.REJECTED || next == TenderScheduleStatus.WITHDRAWN
            TenderScheduleStatus.REJECTED -> return next == TenderScheduleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TenderScheduleStatus) {
        if (!canMoveTo(next)) {
            error("tender schedule $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"tender schedule"} $code"

    fun effectiveVolume(): Double = round((stopCount.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): TenderSchedule {
        return TenderSchedule(
            code = code,
            label = label,
            minimum = minimum,
            stopCount = stopCount,
            pinned = pinned,
            expiresAt = expiresAt,
            shiftCode = shiftCode,
            cost = cost,
            ceiling = ceiling,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tender schedule in status proposed. */
fun newTenderSchedule(code: String, label: String): TenderSchedule {
    return TenderSchedule(
        code = code,
        label = label,
        minimum = 0.0,
        stopCount = 0,
        pinned = false,
        expiresAt = 0,
        shiftCode = "",
        cost = 0.0,
        ceiling = 0.0,
        score = 0.0,
        status = TenderScheduleStatus.PROPOSED,
    )
}
