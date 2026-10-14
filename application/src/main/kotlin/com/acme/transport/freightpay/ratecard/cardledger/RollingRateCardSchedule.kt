package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.kit.text.normalize

/** Where a rolling rate card schedule is in its life cycle. */
enum class RollingRateCardScheduleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rolling rate card schedule. Its code is unique within a tenant. */
data class RollingRateCardSchedule(
    var code: String,
    var label: String,
    var locale: String,
    var windowDays: Int,
    var lane: String,
    var shiftCode: String,
    var pinned: Boolean,
    var status: RollingRateCardScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRateCardScheduleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRateCardScheduleStatus): Boolean {
        when (status) {
            RollingRateCardScheduleStatus.IDLE -> {
                when (next) {
                    RollingRateCardScheduleStatus.DRAINING -> return true
                }
                return false
            }
            RollingRateCardScheduleStatus.WARMING -> {
                when (next) {
                    RollingRateCardScheduleStatus.DRAINING, RollingRateCardScheduleStatus.STOPPED -> return true
                }
                return false
            }
            RollingRateCardScheduleStatus.READY -> {
                when (next) {
                    RollingRateCardScheduleStatus.STOPPED -> return true
                }
                return false
            }
            RollingRateCardScheduleStatus.DRAINING -> {
                when (next) {
                    RollingRateCardScheduleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingRateCardScheduleStatus) {
        if (!canMoveTo(next)) {
            error("rolling rate card schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): RollingRateCardSchedule {
        return RollingRateCardSchedule(
            code = code,
            label = label,
            locale = locale,
            windowDays = windowDays,
            lane = lane,
            shiftCode = shiftCode,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rolling rate card schedule in status idle. */
fun newRollingRateCardSchedule(code: String, label: String): RollingRateCardSchedule {
    return RollingRateCardSchedule(
        code = code,
        label = label,
        locale = "",
        windowDays = 0,
        lane = "",
        shiftCode = "",
        pinned = false,
        status = RollingRateCardScheduleStatus.IDLE,
    )
}
