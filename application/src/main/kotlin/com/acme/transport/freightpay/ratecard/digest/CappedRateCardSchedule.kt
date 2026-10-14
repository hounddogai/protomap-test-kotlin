package com.acme.transport.freightpay.ratecard.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped rate card schedule is in its life cycle. */
enum class CappedRateCardScheduleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One capped rate card schedule, with what the planners need to act on it. */
data class CappedRateCardSchedule(
    var code: String,
    var name: String,
    var ownerId: String,
    var sequence: Int,
    var dock: String,
    var maximum: Double,
    var routeCode: String,
    var visible: Boolean,
    var timezone: String,
    var stopCount: Int,
    var status: CappedRateCardScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRateCardScheduleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRateCardScheduleStatus): Boolean {
        when (status) {
            CappedRateCardScheduleStatus.CREATED -> return next == CappedRateCardScheduleStatus.PUBLISHED || next == CappedRateCardScheduleStatus.RETIRED
            CappedRateCardScheduleStatus.VALIDATED -> return next == CappedRateCardScheduleStatus.PUBLISHED
            CappedRateCardScheduleStatus.PUBLISHED -> return next == CappedRateCardScheduleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CappedRateCardScheduleStatus) {
        if (!canMoveTo(next)) {
            error("capped rate card schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedLoad(): Double = round(stopCount.toDouble() * maximum * 100) / 100

    fun snapshot(): CappedRateCardSchedule {
        return CappedRateCardSchedule(
            code = code,
            name = name,
            ownerId = ownerId,
            sequence = sequence,
            dock = dock,
            maximum = maximum,
            routeCode = routeCode,
            visible = visible,
            timezone = timezone,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped rate card schedule in status created. */
fun newCappedRateCardSchedule(code: String, name: String): CappedRateCardSchedule {
    return CappedRateCardSchedule(
        code = code,
        name = name,
        ownerId = "",
        sequence = 0,
        dock = "",
        maximum = 0.0,
        routeCode = "",
        visible = false,
        timezone = "",
        stopCount = 0,
        status = CappedRateCardScheduleStatus.CREATED,
    )
}
