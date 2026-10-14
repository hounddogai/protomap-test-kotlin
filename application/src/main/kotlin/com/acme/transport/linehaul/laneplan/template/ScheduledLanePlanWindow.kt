package com.acme.transport.linehaul.laneplan.template

import com.acme.transport.kit.text.normalize

/** Where a scheduled lane plan window is in its life cycle. */
enum class ScheduledLanePlanWindowStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled lane plan window as the lane plan team keeps it. */
data class ScheduledLanePlanWindow(
    var code: String,
    var title: String,
    var ownerId: String,
    var depot: String,
    var phone: String,
    var startsAt: Int,
    var status: ScheduledLanePlanWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLanePlanWindowStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLanePlanWindowStatus): Boolean {
        when (status) {
            ScheduledLanePlanWindowStatus.NEW -> return next == ScheduledLanePlanWindowStatus.RUNNING || next == ScheduledLanePlanWindowStatus.FAILED
            ScheduledLanePlanWindowStatus.QUEUED -> return next == ScheduledLanePlanWindowStatus.DONE || next == ScheduledLanePlanWindowStatus.FAILED
            ScheduledLanePlanWindowStatus.RUNNING -> {
                when (next) {
                    ScheduledLanePlanWindowStatus.DONE, ScheduledLanePlanWindowStatus.FAILED -> return true
                }
                return false
            }
            ScheduledLanePlanWindowStatus.DONE -> return next == ScheduledLanePlanWindowStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledLanePlanWindowStatus) {
        if (!canMoveTo(next)) {
            error("scheduled lane plan window $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): ScheduledLanePlanWindow {
        return ScheduledLanePlanWindow(
            code = code,
            title = title,
            ownerId = ownerId,
            depot = depot,
            phone = phone,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scheduled lane plan window in status new. */
fun newScheduledLanePlanWindow(code: String, title: String): ScheduledLanePlanWindow {
    return ScheduledLanePlanWindow(
        code = code,
        title = title,
        ownerId = "",
        depot = "",
        phone = "",
        startsAt = 0,
        status = ScheduledLanePlanWindowStatus.NEW,
    )
}
