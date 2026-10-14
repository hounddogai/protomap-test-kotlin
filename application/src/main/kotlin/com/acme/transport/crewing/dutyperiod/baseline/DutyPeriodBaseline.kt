package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The duty period baseline records of crewing's duty period work, and the rules, stores, and calculations
// around them.

/** Where a duty period baseline is in its life cycle. */
enum class DutyPeriodBaselineStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One duty period baseline, with what the planners need to act on it. */
data class DutyPeriodBaseline(
    var code: String,
    var title: String,
    var segment: String,
    var tags: MutableList<String>,
    var externalRef: String,
    var attributes: MutableMap<String, String>,
    var durationMinutes: Int,
    var odometer: Int,
    var locale: String,
    var active: Boolean,
    var status: DutyPeriodBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodBaselineStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodBaselineStatus): Boolean {
        when (status) {
            DutyPeriodBaselineStatus.OPEN -> return next == DutyPeriodBaselineStatus.IN_REVIEW || next == DutyPeriodBaselineStatus.ARCHIVED
            DutyPeriodBaselineStatus.IN_REVIEW -> return next == DutyPeriodBaselineStatus.RESOLVED
            DutyPeriodBaselineStatus.RESOLVED -> return next == DutyPeriodBaselineStatus.REOPENED
            DutyPeriodBaselineStatus.REOPENED -> {
                when (next) {
                    DutyPeriodBaselineStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodBaselineStatus) {
        if (!canMoveTo(next)) {
            error("duty period baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round((odometer.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): DutyPeriodBaseline {
        return DutyPeriodBaseline(
            code = code,
            title = title,
            segment = segment,
            tags = tags,
            externalRef = externalRef,
            attributes = attributes,
            durationMinutes = durationMinutes,
            odometer = odometer,
            locale = locale,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a duty period baseline in status open. */
fun newDutyPeriodBaseline(code: String, title: String): DutyPeriodBaseline {
    return DutyPeriodBaseline(
        code = code,
        title = title,
        segment = "",
        tags = mutableListOf<String>(),
        externalRef = "",
        attributes = mutableMapOf<String, String>(),
        durationMinutes = 0,
        odometer = 0,
        locale = "",
        active = false,
        status = DutyPeriodBaselineStatus.OPEN,
    )
}
