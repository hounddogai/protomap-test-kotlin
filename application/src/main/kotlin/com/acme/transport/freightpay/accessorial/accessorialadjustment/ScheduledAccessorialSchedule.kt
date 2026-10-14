package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled accessorial schedule is in its life cycle. */
enum class ScheduledAccessorialScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled accessorial schedule. Its code is unique within a tenant. */
data class ScheduledAccessorialSchedule(
    var code: String,
    var title: String,
    var lane: String,
    var floorValue: Double,
    var attributes: MutableMap<String, String>,
    var visible: Boolean,
    var rate: Double,
    var windowDays: Int,
    var discount: Double,
    var dock: String,
    var status: ScheduledAccessorialScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledAccessorialScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledAccessorialScheduleStatus): Boolean {
        when (status) {
            ScheduledAccessorialScheduleStatus.DRAFT -> {
                when (next) {
                    ScheduledAccessorialScheduleStatus.ACTIVE, ScheduledAccessorialScheduleStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledAccessorialScheduleStatus.PENDING -> {
                when (next) {
                    ScheduledAccessorialScheduleStatus.ACTIVE -> return true
                }
                return false
            }
            ScheduledAccessorialScheduleStatus.ACTIVE -> return next == ScheduledAccessorialScheduleStatus.CLOSED
            ScheduledAccessorialScheduleStatus.SUSPENDED -> return next == ScheduledAccessorialScheduleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScheduledAccessorialScheduleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled accessorial schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedExposure(): Double = round((windowDays.toDouble() + floorValue) * 100) / 100

    fun snapshot(): ScheduledAccessorialSchedule {
        return ScheduledAccessorialSchedule(
            code = code,
            title = title,
            lane = lane,
            floorValue = floorValue,
            attributes = attributes,
            visible = visible,
            rate = rate,
            windowDays = windowDays,
            discount = discount,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a scheduled accessorial schedule in status draft. */
fun newScheduledAccessorialSchedule(code: String, title: String): ScheduledAccessorialSchedule {
    return ScheduledAccessorialSchedule(
        code = code,
        title = title,
        lane = "",
        floorValue = 0.0,
        attributes = mutableMapOf<String, String>(),
        visible = false,
        rate = 0.0,
        windowDays = 0,
        discount = 0.0,
        dock = "",
        status = ScheduledAccessorialScheduleStatus.DRAFT,
    )
}
