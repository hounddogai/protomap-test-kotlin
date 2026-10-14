package com.acme.transport.emissions.idling.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling threshold is in its life cycle. */
enum class IdlingThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A idling threshold. Its code is unique within a tenant. */
data class IdlingThreshold(
    var code: String,
    var title: String,
    var minimum: Double,
    var rate: Double,
    var name: String,
    var graceDays: Int,
    var externalRef: String,
    var priority: Int,
    var segment: String,
    var status: IdlingThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingThresholdStatus): Boolean {
        when (status) {
            IdlingThresholdStatus.PROPOSED -> return next == IdlingThresholdStatus.APPROVED
            IdlingThresholdStatus.APPROVED -> return next == IdlingThresholdStatus.REJECTED
            IdlingThresholdStatus.REJECTED -> {
                when (next) {
                    IdlingThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingThresholdStatus) {
        if (!canMoveTo(next)) {
            error("idling threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedValue(): Double = round((graceDays.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): IdlingThreshold {
        return IdlingThreshold(
            code = code,
            title = title,
            minimum = minimum,
            rate = rate,
            name = name,
            graceDays = graceDays,
            externalRef = externalRef,
            priority = priority,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a idling threshold in status proposed. */
fun newIdlingThreshold(code: String, title: String): IdlingThreshold {
    return IdlingThreshold(
        code = code,
        title = title,
        minimum = 0.0,
        rate = 0.0,
        name = "",
        graceDays = 0,
        externalRef = "",
        priority = 0,
        segment = "",
        status = IdlingThresholdStatus.PROPOSED,
    )
}
