package com.acme.transport.customs.hold.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hold schedule records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a hold schedule is in its life cycle. */
enum class HoldScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One hold schedule, with what the planners need to act on it. */
data class HoldSchedule(
    var code: String,
    var title: String,
    var contactEmail: String,
    var priority: Int,
    var trailerId: String,
    var batchSize: Int,
    var status: HoldScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldScheduleStatus): Boolean {
        when (status) {
            HoldScheduleStatus.DRAFT -> {
                when (next) {
                    HoldScheduleStatus.ACTIVE -> return true
                }
                return false
            }
            HoldScheduleStatus.PENDING -> {
                when (next) {
                    HoldScheduleStatus.SUSPENDED -> return true
                }
                return false
            }
            HoldScheduleStatus.ACTIVE -> return next == HoldScheduleStatus.SUSPENDED
            HoldScheduleStatus.SUSPENDED -> return next == HoldScheduleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: HoldScheduleStatus) {
        if (!canMoveTo(next)) {
            error("hold schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveLoad(): Double = round(batchSize.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): HoldSchedule {
        return HoldSchedule(
            code = code,
            title = title,
            contactEmail = contactEmail,
            priority = priority,
            trailerId = trailerId,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a hold schedule in status draft. */
fun newHoldSchedule(code: String, title: String): HoldSchedule {
    return HoldSchedule(
        code = code,
        title = title,
        contactEmail = "",
        priority = 0,
        trailerId = "",
        batchSize = 0,
        status = HoldScheduleStatus.DRAFT,
    )
}
