package com.acme.transport.terminal.dockdoor.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled dock door policy is in its life cycle. */
enum class ScheduledDockDoorPolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A scheduled dock door policy. Its code is unique within a tenant. */
data class ScheduledDockDoorPolicy(
    var code: String,
    var title: String,
    var ceiling: Double,
    var threshold: Double,
    var segment: String,
    var approved: Boolean,
    var ratio: Double,
    var region: String,
    var windowDays: Int,
    var odometer: Int,
    var status: ScheduledDockDoorPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDockDoorPolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDockDoorPolicyStatus): Boolean {
        when (status) {
            ScheduledDockDoorPolicyStatus.PROPOSED -> return next == ScheduledDockDoorPolicyStatus.WITHDRAWN
            ScheduledDockDoorPolicyStatus.APPROVED -> return next == ScheduledDockDoorPolicyStatus.REJECTED
            ScheduledDockDoorPolicyStatus.REJECTED -> {
                when (next) {
                    ScheduledDockDoorPolicyStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledDockDoorPolicyStatus) {
        if (!canMoveTo(next)) {
            error("scheduled dock door policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netLoad(): Double = round((windowDays.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): ScheduledDockDoorPolicy {
        return ScheduledDockDoorPolicy(
            code = code,
            title = title,
            ceiling = ceiling,
            threshold = threshold,
            segment = segment,
            approved = approved,
            ratio = ratio,
            region = region,
            windowDays = windowDays,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a scheduled dock door policy in status proposed. */
fun newScheduledDockDoorPolicy(code: String, title: String): ScheduledDockDoorPolicy {
    return ScheduledDockDoorPolicy(
        code = code,
        title = title,
        ceiling = 0.0,
        threshold = 0.0,
        segment = "",
        approved = false,
        ratio = 0.0,
        region = "",
        windowDays = 0,
        odometer = 0,
        status = ScheduledDockDoorPolicyStatus.PROPOSED,
    )
}
