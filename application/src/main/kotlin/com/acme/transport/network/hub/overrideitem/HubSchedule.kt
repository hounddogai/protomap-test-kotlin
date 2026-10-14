package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub schedule is in its life cycle. */
enum class HubScheduleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A hub schedule as the hub team keeps it. */
data class HubSchedule(
    var code: String,
    var title: String,
    var timezone: String,
    var createdAt: Int,
    var discount: Double,
    var minimum: Double,
    var status: HubScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubScheduleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: HubScheduleStatus): Boolean {
        when (status) {
            HubScheduleStatus.PROPOSED -> {
                when (next) {
                    HubScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
            HubScheduleStatus.APPROVED -> {
                when (next) {
                    HubScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
            HubScheduleStatus.REJECTED -> return next == HubScheduleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: HubScheduleStatus) {
        if (!canMoveTo(next)) {
            error("hub schedule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netExposure(): Double = round((discount - createdAt.toDouble()) * 100) / 100

    fun snapshot(): HubSchedule {
        return HubSchedule(
            code = code,
            title = title,
            timezone = timezone,
            createdAt = createdAt,
            discount = discount,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a hub schedule in status proposed. */
fun newHubSchedule(code: String, title: String): HubSchedule {
    return HubSchedule(
        code = code,
        title = title,
        timezone = "",
        createdAt = 0,
        discount = 0.0,
        minimum = 0.0,
        status = HubScheduleStatus.PROPOSED,
    )
}
