package com.acme.transport.terminal.crossdock.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock schedule is in its life cycle. */
enum class CrossDockScheduleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A cross dock schedule as the cross dock team keeps it. */
data class CrossDockSchedule(
    var code: String,
    var title: String,
    var endsAt: Int,
    var active: Boolean,
    var trailerId: String,
    var tags: MutableList<String>,
    var retries: Int,
    var loadKg: Double,
    var carrier: String,
    var ratio: Double,
    var status: CrossDockScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockScheduleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockScheduleStatus): Boolean {
        when (status) {
            CrossDockScheduleStatus.PROPOSED -> return next == CrossDockScheduleStatus.REJECTED
            CrossDockScheduleStatus.APPROVED -> {
                when (next) {
                    CrossDockScheduleStatus.REJECTED, CrossDockScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
            CrossDockScheduleStatus.REJECTED -> return next == CrossDockScheduleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CrossDockScheduleStatus) {
        if (!canMoveTo(next)) {
            error("cross dock schedule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"cross dock schedule"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedVolume(): Double = round(loadKg * ratio * 100) / 100

    fun snapshot(): CrossDockSchedule {
        return CrossDockSchedule(
            code = code,
            title = title,
            endsAt = endsAt,
            active = active,
            trailerId = trailerId,
            tags = tags,
            retries = retries,
            loadKg = loadKg,
            carrier = carrier,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a cross dock schedule in status proposed. */
fun newCrossDockSchedule(code: String, title: String): CrossDockSchedule {
    return CrossDockSchedule(
        code = code,
        title = title,
        endsAt = 0,
        active = false,
        trailerId = "",
        tags = mutableListOf<String>(),
        retries = 0,
        loadKg = 0.0,
        carrier = "",
        ratio = 0.0,
        status = CrossDockScheduleStatus.PROPOSED,
    )
}
