package com.acme.transport.emissions.carbonreport.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default carbon report milestone is in its life cycle. */
enum class DefaultCarbonReportMilestoneStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A default carbon report milestone. Its code is unique within a tenant. */
data class DefaultCarbonReportMilestone(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var graceDays: Int,
    var startsAt: Int,
    var margin: Double,
    var active: Boolean,
    var status: DefaultCarbonReportMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCarbonReportMilestoneStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCarbonReportMilestoneStatus): Boolean {
        when (status) {
            DefaultCarbonReportMilestoneStatus.PLANNED -> {
                when (next) {
                    DefaultCarbonReportMilestoneStatus.IN_PROGRESS, DefaultCarbonReportMilestoneStatus.COMPLETED -> return true
                }
                return false
            }
            DefaultCarbonReportMilestoneStatus.SCHEDULED -> {
                when (next) {
                    DefaultCarbonReportMilestoneStatus.IN_PROGRESS, DefaultCarbonReportMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultCarbonReportMilestoneStatus.IN_PROGRESS -> {
                when (next) {
                    DefaultCarbonReportMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultCarbonReportMilestoneStatus.COMPLETED -> {
                when (next) {
                    DefaultCarbonReportMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultCarbonReportMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("default carbon report milestone $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default carbon report milestone"} $code"

    fun netLoad(): Double = round((durationMinutes.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultCarbonReportMilestone {
        return DefaultCarbonReportMilestone(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            graceDays = graceDays,
            startsAt = startsAt,
            margin = margin,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default carbon report milestone in status planned. */
fun newDefaultCarbonReportMilestone(code: String, title: String): DefaultCarbonReportMilestone {
    return DefaultCarbonReportMilestone(
        code = code,
        title = title,
        durationMinutes = 0,
        graceDays = 0,
        startsAt = 0,
        margin = 0.0,
        active = false,
        status = DefaultCarbonReportMilestoneStatus.PLANNED,
    )
}
