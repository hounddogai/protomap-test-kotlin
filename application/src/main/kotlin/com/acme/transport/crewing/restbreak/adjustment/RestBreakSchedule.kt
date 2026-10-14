package com.acme.transport.crewing.restbreak.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break schedule is in its life cycle. */
enum class RestBreakScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rest break schedule as the rest break team keeps it. */
data class RestBreakSchedule(
    var code: String,
    var label: String,
    var description: String,
    var channel: String,
    var cost: Double,
    var carrier: String,
    var summary: String,
    var archived: Boolean,
    var weight: Double,
    var dock: String,
    var tags: MutableList<String>,
    var status: RestBreakScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakScheduleStatus): Boolean {
        when (status) {
            RestBreakScheduleStatus.DRAFT -> {
                when (next) {
                    RestBreakScheduleStatus.SUSPENDED -> return true
                }
                return false
            }
            RestBreakScheduleStatus.PENDING -> return next == RestBreakScheduleStatus.SUSPENDED || next == RestBreakScheduleStatus.CLOSED
            RestBreakScheduleStatus.ACTIVE -> {
                when (next) {
                    RestBreakScheduleStatus.SUSPENDED -> return true
                }
                return false
            }
            RestBreakScheduleStatus.SUSPENDED -> {
                when (next) {
                    RestBreakScheduleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakScheduleStatus) {
        if (!canMoveTo(next)) {
            error("rest break schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rest break schedule"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netVolume(): Double = round((cost - weight) * 100) / 100

    fun snapshot(): RestBreakSchedule {
        return RestBreakSchedule(
            code = code,
            label = label,
            description = description,
            channel = channel,
            cost = cost,
            carrier = carrier,
            summary = summary,
            archived = archived,
            weight = weight,
            dock = dock,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rest break schedule in status draft. */
fun newRestBreakSchedule(code: String, label: String): RestBreakSchedule {
    return RestBreakSchedule(
        code = code,
        label = label,
        description = "",
        channel = "",
        cost = 0.0,
        carrier = "",
        summary = "",
        archived = false,
        weight = 0.0,
        dock = "",
        tags = mutableListOf<String>(),
        status = RestBreakScheduleStatus.DRAFT,
    )
}
