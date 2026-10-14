package com.acme.transport.terminal.dwell.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell schedule is in its life cycle. */
enum class DwellScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A dwell schedule. Its code is unique within a tenant. */
data class DwellSchedule(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var country: String,
    var quantity: Int,
    var segment: String,
    var reviewer: String,
    var durationMinutes: Int,
    var status: DwellScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellScheduleStatus): Boolean {
        when (status) {
            DwellScheduleStatus.OPEN -> return next == DwellScheduleStatus.IN_REVIEW || next == DwellScheduleStatus.REOPENED
            DwellScheduleStatus.IN_REVIEW -> {
                when (next) {
                    DwellScheduleStatus.RESOLVED, DwellScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
            DwellScheduleStatus.RESOLVED -> {
                when (next) {
                    DwellScheduleStatus.REOPENED -> return true
                }
                return false
            }
            DwellScheduleStatus.REOPENED -> {
                when (next) {
                    DwellScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellScheduleStatus) {
        if (!canMoveTo(next)) {
            error("dwell schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveTotal(): Double = round((durationMinutes.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): DwellSchedule {
        return DwellSchedule(
            code = code,
            label = label,
            tags = tags,
            country = country,
            quantity = quantity,
            segment = segment,
            reviewer = reviewer,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a dwell schedule in status open. */
fun newDwellSchedule(code: String, label: String): DwellSchedule {
    return DwellSchedule(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        country = "",
        quantity = 0,
        segment = "",
        reviewer = "",
        durationMinutes = 0,
        status = DwellScheduleStatus.OPEN,
    )
}
