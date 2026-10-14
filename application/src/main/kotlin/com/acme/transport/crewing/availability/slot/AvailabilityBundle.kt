package com.acme.transport.crewing.availability.slot

import com.acme.transport.kit.text.normalize

/** Where a availability bundle is in its life cycle. */
enum class AvailabilityBundleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A availability bundle. Its code is unique within a tenant. */
data class AvailabilityBundle(
    var code: String,
    var label: String,
    var active: Boolean,
    var windowDays: Int,
    var depot: String,
    var lane: String,
    var status: AvailabilityBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityBundleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityBundleStatus): Boolean {
        when (status) {
            AvailabilityBundleStatus.PROPOSED -> return next == AvailabilityBundleStatus.APPROVED || next == AvailabilityBundleStatus.REJECTED
            AvailabilityBundleStatus.APPROVED -> {
                when (next) {
                    AvailabilityBundleStatus.REJECTED, AvailabilityBundleStatus.WITHDRAWN -> return true
                }
                return false
            }
            AvailabilityBundleStatus.REJECTED -> return next == AvailabilityBundleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AvailabilityBundleStatus) {
        if (!canMoveTo(next)) {
            error("availability bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"availability bundle"} $code"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): AvailabilityBundle {
        return AvailabilityBundle(
            code = code,
            label = label,
            active = active,
            windowDays = windowDays,
            depot = depot,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a availability bundle in status proposed. */
fun newAvailabilityBundle(code: String, label: String): AvailabilityBundle {
    return AvailabilityBundle(
        code = code,
        label = label,
        active = false,
        windowDays = 0,
        depot = "",
        lane = "",
        status = AvailabilityBundleStatus.PROPOSED,
    )
}
