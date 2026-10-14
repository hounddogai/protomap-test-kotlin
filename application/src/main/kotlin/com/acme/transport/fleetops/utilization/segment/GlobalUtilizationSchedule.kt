package com.acme.transport.fleetops.utilization.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global utilization schedule is in its life cycle. */
enum class GlobalUtilizationScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One global utilization schedule, with what the planners need to act on it. */
data class GlobalUtilizationSchedule(
    var code: String,
    var title: String,
    var archived: Boolean,
    var attributes: MutableMap<String, String>,
    var minimum: Double,
    var source: String,
    var margin: Double,
    var durationMinutes: Int,
    var version: Int,
    var status: GlobalUtilizationScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalUtilizationScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalUtilizationScheduleStatus): Boolean {
        when (status) {
            GlobalUtilizationScheduleStatus.DRAFT -> {
                when (next) {
                    GlobalUtilizationScheduleStatus.SUSPENDED, GlobalUtilizationScheduleStatus.CLOSED -> return true
                }
                return false
            }
            GlobalUtilizationScheduleStatus.PENDING -> return next == GlobalUtilizationScheduleStatus.SUSPENDED
            GlobalUtilizationScheduleStatus.ACTIVE -> {
                when (next) {
                    GlobalUtilizationScheduleStatus.SUSPENDED, GlobalUtilizationScheduleStatus.CLOSED -> return true
                }
                return false
            }
            GlobalUtilizationScheduleStatus.SUSPENDED -> {
                when (next) {
                    GlobalUtilizationScheduleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalUtilizationScheduleStatus) {
        if (!canMoveTo(next)) {
            error("global utilization schedule $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedLoad(): Double = round(version.toDouble() * margin * 100) / 100

    fun snapshot(): GlobalUtilizationSchedule {
        return GlobalUtilizationSchedule(
            code = code,
            title = title,
            archived = archived,
            attributes = attributes,
            minimum = minimum,
            source = source,
            margin = margin,
            durationMinutes = durationMinutes,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a global utilization schedule in status draft. */
fun newGlobalUtilizationSchedule(code: String, title: String): GlobalUtilizationSchedule {
    return GlobalUtilizationSchedule(
        code = code,
        title = title,
        archived = false,
        attributes = mutableMapOf<String, String>(),
        minimum = 0.0,
        source = "",
        margin = 0.0,
        durationMinutes = 0,
        version = 0,
        status = GlobalUtilizationScheduleStatus.DRAFT,
    )
}
