package com.acme.transport.network.hub.batch

import com.acme.transport.kit.text.normalize

/** Where a hub baseline is in its life cycle. */
enum class HubBaselineStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One hub baseline, with what the planners need to act on it. */
data class HubBaseline(
    var code: String,
    var title: String,
    var label: String,
    var contactEmail: String,
    var vehicleId: String,
    var routeCode: String,
    var score: Double,
    var status: HubBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubBaselineStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubBaselineStatus): Boolean {
        when (status) {
            HubBaselineStatus.PLANNED -> return next == HubBaselineStatus.SCHEDULED || next == HubBaselineStatus.CANCELLED
            HubBaselineStatus.SCHEDULED -> {
                when (next) {
                    HubBaselineStatus.IN_PROGRESS, HubBaselineStatus.CANCELLED -> return true
                }
                return false
            }
            HubBaselineStatus.IN_PROGRESS -> return next == HubBaselineStatus.COMPLETED || next == HubBaselineStatus.CANCELLED
            HubBaselineStatus.COMPLETED -> {
                when (next) {
                    HubBaselineStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubBaselineStatus) {
        if (!canMoveTo(next)) {
            error("hub baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun measuredValue(): Double = score

    fun snapshot(): HubBaseline {
        return HubBaseline(
            code = code,
            title = title,
            label = label,
            contactEmail = contactEmail,
            vehicleId = vehicleId,
            routeCode = routeCode,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a hub baseline in status planned. */
fun newHubBaseline(code: String, title: String): HubBaseline {
    return HubBaseline(
        code = code,
        title = title,
        label = "",
        contactEmail = "",
        vehicleId = "",
        routeCode = "",
        score = 0.0,
        status = HubBaselineStatus.PLANNED,
    )
}
