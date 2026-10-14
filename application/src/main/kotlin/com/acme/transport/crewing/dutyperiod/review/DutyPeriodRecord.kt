package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period record is in its life cycle. */
enum class DutyPeriodRecordStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a duty period record and its current status. */
data class DutyPeriodRecord(
    var code: String,
    var name: String,
    var category: String,
    var tags: MutableList<String>,
    var limit: Int,
    var routeCode: String,
    var expiresAt: Int,
    var archived: Boolean,
    var windowDays: Int,
    var driverId: String,
    var graceDays: Int,
    var status: DutyPeriodRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodRecordStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodRecordStatus): Boolean {
        when (status) {
            DutyPeriodRecordStatus.PLANNED -> return next == DutyPeriodRecordStatus.IN_PROGRESS || next == DutyPeriodRecordStatus.CANCELLED
            DutyPeriodRecordStatus.SCHEDULED -> return next == DutyPeriodRecordStatus.COMPLETED
            DutyPeriodRecordStatus.IN_PROGRESS -> {
                when (next) {
                    DutyPeriodRecordStatus.COMPLETED, DutyPeriodRecordStatus.CANCELLED -> return true
                }
                return false
            }
            DutyPeriodRecordStatus.COMPLETED -> {
                when (next) {
                    DutyPeriodRecordStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodRecordStatus) {
        if (!canMoveTo(next)) {
            error("duty period record $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedTotal(): Double = round(limit.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): DutyPeriodRecord {
        return DutyPeriodRecord(
            code = code,
            name = name,
            category = category,
            tags = tags,
            limit = limit,
            routeCode = routeCode,
            expiresAt = expiresAt,
            archived = archived,
            windowDays = windowDays,
            driverId = driverId,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a duty period record in status planned. */
fun newDutyPeriodRecord(code: String, name: String): DutyPeriodRecord {
    return DutyPeriodRecord(
        code = code,
        name = name,
        category = "",
        tags = mutableListOf<String>(),
        limit = 0,
        routeCode = "",
        expiresAt = 0,
        archived = false,
        windowDays = 0,
        driverId = "",
        graceDays = 0,
        status = DutyPeriodRecordStatus.PLANNED,
    )
}
