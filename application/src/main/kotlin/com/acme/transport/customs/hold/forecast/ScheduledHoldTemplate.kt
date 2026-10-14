package com.acme.transport.customs.hold.forecast

import com.acme.transport.kit.text.normalize

/** Where a scheduled hold template is in its life cycle. */
enum class ScheduledHoldTemplateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One scheduled hold template, with what the planners need to act on it. */
data class ScheduledHoldTemplate(
    var code: String,
    var title: String,
    var routeCode: String,
    var createdAt: Int,
    var vehicleId: String,
    var status: ScheduledHoldTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledHoldTemplateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledHoldTemplateStatus): Boolean {
        when (status) {
            ScheduledHoldTemplateStatus.PROPOSED -> return next == ScheduledHoldTemplateStatus.APPROVED || next == ScheduledHoldTemplateStatus.WITHDRAWN
            ScheduledHoldTemplateStatus.APPROVED -> {
                when (next) {
                    ScheduledHoldTemplateStatus.REJECTED -> return true
                }
                return false
            }
            ScheduledHoldTemplateStatus.REJECTED -> {
                when (next) {
                    ScheduledHoldTemplateStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledHoldTemplateStatus) {
        if (!canMoveTo(next)) {
            error("scheduled hold template $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"scheduled hold template"} $code"

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): ScheduledHoldTemplate {
        return ScheduledHoldTemplate(
            code = code,
            title = title,
            routeCode = routeCode,
            createdAt = createdAt,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled hold template in status proposed. */
fun newScheduledHoldTemplate(code: String, title: String): ScheduledHoldTemplate {
    return ScheduledHoldTemplate(
        code = code,
        title = title,
        routeCode = "",
        createdAt = 0,
        vehicleId = "",
        status = ScheduledHoldTemplateStatus.PROPOSED,
    )
}
