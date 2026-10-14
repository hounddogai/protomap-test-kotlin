package com.acme.transport.linehaul.capacity.capacitytarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped capacity schedule is in its life cycle. */
enum class CappedCapacityScheduleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a capped capacity schedule and its current status. */
data class CappedCapacitySchedule(
    var code: String,
    var title: String,
    var category: String,
    var threshold: Double,
    var depot: String,
    var windowDays: Int,
    var endsAt: Int,
    var channel: String,
    var externalRef: String,
    var status: CappedCapacityScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCapacityScheduleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCapacityScheduleStatus): Boolean {
        when (status) {
            CappedCapacityScheduleStatus.REQUESTED -> return next == CappedCapacityScheduleStatus.GRANTED || next == CappedCapacityScheduleStatus.EXPIRED
            CappedCapacityScheduleStatus.GRANTED -> return next == CappedCapacityScheduleStatus.EXPIRED
            CappedCapacityScheduleStatus.REVOKED -> {
                when (next) {
                    CappedCapacityScheduleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedCapacityScheduleStatus) {
        if (!canMoveTo(next)) {
            error("capped capacity schedule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"capped capacity schedule"} $code"

    fun adjustedExposure(): Double = round(threshold * endsAt.toDouble() * 100) / 100

    fun snapshot(): CappedCapacitySchedule {
        return CappedCapacitySchedule(
            code = code,
            title = title,
            category = category,
            threshold = threshold,
            depot = depot,
            windowDays = windowDays,
            endsAt = endsAt,
            channel = channel,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped capacity schedule in status requested. */
fun newCappedCapacitySchedule(code: String, title: String): CappedCapacitySchedule {
    return CappedCapacitySchedule(
        code = code,
        title = title,
        category = "",
        threshold = 0.0,
        depot = "",
        windowDays = 0,
        endsAt = 0,
        channel = "",
        externalRef = "",
        status = CappedCapacityScheduleStatus.REQUESTED,
    )
}
