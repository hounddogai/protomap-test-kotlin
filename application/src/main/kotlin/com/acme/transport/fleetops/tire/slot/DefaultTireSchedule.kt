package com.acme.transport.fleetops.tire.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default tire schedule is in its life cycle. */
enum class DefaultTireScheduleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One default tire schedule, with what the planners need to act on it. */
data class DefaultTireSchedule(
    var code: String,
    var name: String,
    var contactEmail: String,
    var graceDays: Int,
    var discount: Double,
    var reviewer: String,
    var batchSize: Int,
    var phone: String,
    var windowDays: Int,
    var depot: String,
    var status: DefaultTireScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTireScheduleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTireScheduleStatus): Boolean {
        when (status) {
            DefaultTireScheduleStatus.NEW -> return next == DefaultTireScheduleStatus.QUEUED || next == DefaultTireScheduleStatus.FAILED
            DefaultTireScheduleStatus.QUEUED -> return next == DefaultTireScheduleStatus.RUNNING || next == DefaultTireScheduleStatus.DONE
            DefaultTireScheduleStatus.RUNNING -> {
                when (next) {
                    DefaultTireScheduleStatus.DONE, DefaultTireScheduleStatus.FAILED -> return true
                }
                return false
            }
            DefaultTireScheduleStatus.DONE -> return next == DefaultTireScheduleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DefaultTireScheduleStatus) {
        if (!canMoveTo(next)) {
            error("default tire schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netValue(): Double = round(batchSize.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): DefaultTireSchedule {
        return DefaultTireSchedule(
            code = code,
            name = name,
            contactEmail = contactEmail,
            graceDays = graceDays,
            discount = discount,
            reviewer = reviewer,
            batchSize = batchSize,
            phone = phone,
            windowDays = windowDays,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a default tire schedule in status new. */
fun newDefaultTireSchedule(code: String, name: String): DefaultTireSchedule {
    return DefaultTireSchedule(
        code = code,
        name = name,
        contactEmail = "",
        graceDays = 0,
        discount = 0.0,
        reviewer = "",
        batchSize = 0,
        phone = "",
        windowDays = 0,
        depot = "",
        status = DefaultTireScheduleStatus.NEW,
    )
}
