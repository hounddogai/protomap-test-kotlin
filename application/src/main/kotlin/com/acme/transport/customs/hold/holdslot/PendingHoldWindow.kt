package com.acme.transport.customs.hold.holdslot

import com.acme.transport.kit.text.normalize

/** Where a pending hold window is in its life cycle. */
enum class PendingHoldWindowStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A pending hold window as the hold team keeps it. */
data class PendingHoldWindow(
    var code: String,
    var label: String,
    var maximum: Double,
    var contactEmail: String,
    var reviewer: String,
    var status: PendingHoldWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingHoldWindowStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingHoldWindowStatus): Boolean {
        when (status) {
            PendingHoldWindowStatus.CREATED -> {
                when (next) {
                    PendingHoldWindowStatus.VALIDATED, PendingHoldWindowStatus.PUBLISHED -> return true
                }
                return false
            }
            PendingHoldWindowStatus.VALIDATED -> {
                when (next) {
                    PendingHoldWindowStatus.PUBLISHED, PendingHoldWindowStatus.RETIRED -> return true
                }
                return false
            }
            PendingHoldWindowStatus.PUBLISHED -> return next == PendingHoldWindowStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PendingHoldWindowStatus) {
        if (!canMoveTo(next)) {
            error("pending hold window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending hold window"} $code"

    fun measuredValue(): Double = maximum

    fun snapshot(): PendingHoldWindow {
        return PendingHoldWindow(
            code = code,
            label = label,
            maximum = maximum,
            contactEmail = contactEmail,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending hold window in status created. */
fun newPendingHoldWindow(code: String, label: String): PendingHoldWindow {
    return PendingHoldWindow(
        code = code,
        label = label,
        maximum = 0.0,
        contactEmail = "",
        reviewer = "",
        status = PendingHoldWindowStatus.CREATED,
    )
}
