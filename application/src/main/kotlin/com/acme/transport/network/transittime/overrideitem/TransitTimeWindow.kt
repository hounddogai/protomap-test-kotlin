package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time window is in its life cycle. */
enum class TransitTimeWindowStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One transit time window, with what the planners need to act on it. */
data class TransitTimeWindow(
    var code: String,
    var label: String,
    var contactEmail: String,
    var budget: Double,
    var attempts: Int,
    var status: TransitTimeWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeWindowStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeWindowStatus): Boolean {
        when (status) {
            TransitTimeWindowStatus.CREATED -> return next == TransitTimeWindowStatus.VALIDATED
            TransitTimeWindowStatus.VALIDATED -> {
                when (next) {
                    TransitTimeWindowStatus.PUBLISHED -> return true
                }
                return false
            }
            TransitTimeWindowStatus.PUBLISHED -> {
                when (next) {
                    TransitTimeWindowStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeWindowStatus) {
        if (!canMoveTo(next)) {
            error("transit time window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedValue(): Double = round(attempts.toDouble() * budget * 100) / 100

    fun snapshot(): TransitTimeWindow {
        return TransitTimeWindow(
            code = code,
            label = label,
            contactEmail = contactEmail,
            budget = budget,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a transit time window in status created. */
fun newTransitTimeWindow(code: String, label: String): TransitTimeWindow {
    return TransitTimeWindow(
        code = code,
        label = label,
        contactEmail = "",
        budget = 0.0,
        attempts = 0,
        status = TransitTimeWindowStatus.CREATED,
    )
}
