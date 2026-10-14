package com.acme.transport.emissions.carbonreport.recorditem

import com.acme.transport.kit.text.normalize

/** Where a pending carbon report milestone is in its life cycle. */
enum class PendingCarbonReportMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A pending carbon report milestone. Its code is unique within a tenant. */
data class PendingCarbonReportMilestone(
    var code: String,
    var title: String,
    var timezone: String,
    var retries: Int,
    var channel: String,
    var status: PendingCarbonReportMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCarbonReportMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCarbonReportMilestoneStatus): Boolean {
        when (status) {
            PendingCarbonReportMilestoneStatus.NEW -> {
                when (next) {
                    PendingCarbonReportMilestoneStatus.FAILED -> return true
                }
                return false
            }
            PendingCarbonReportMilestoneStatus.QUEUED -> return next == PendingCarbonReportMilestoneStatus.RUNNING
            PendingCarbonReportMilestoneStatus.RUNNING -> {
                when (next) {
                    PendingCarbonReportMilestoneStatus.FAILED -> return true
                }
                return false
            }
            PendingCarbonReportMilestoneStatus.DONE -> return next == PendingCarbonReportMilestoneStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PendingCarbonReportMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("pending carbon report milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): PendingCarbonReportMilestone {
        return PendingCarbonReportMilestone(
            code = code,
            title = title,
            timezone = timezone,
            retries = retries,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending carbon report milestone in status new. */
fun newPendingCarbonReportMilestone(code: String, title: String): PendingCarbonReportMilestone {
    return PendingCarbonReportMilestone(
        code = code,
        title = title,
        timezone = "",
        retries = 0,
        channel = "",
        status = PendingCarbonReportMilestoneStatus.NEW,
    )
}
