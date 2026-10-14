package com.acme.transport.emissions.idling.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling slot is in its life cycle. */
enum class IdlingSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a idling slot and its current status. */
data class IdlingSlot(
    var code: String,
    var title: String,
    var visible: Boolean,
    var threshold: Double,
    var unit: String,
    var contactEmail: String,
    var version: Int,
    var depot: String,
    var shiftCode: String,
    var score: Double,
    var status: IdlingSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingSlotStatus): Boolean {
        when (status) {
            IdlingSlotStatus.OPEN -> {
                when (next) {
                    IdlingSlotStatus.ARCHIVED -> return true
                }
                return false
            }
            IdlingSlotStatus.IN_REVIEW -> return next == IdlingSlotStatus.RESOLVED || next == IdlingSlotStatus.REOPENED
            IdlingSlotStatus.RESOLVED -> return next == IdlingSlotStatus.REOPENED || next == IdlingSlotStatus.ARCHIVED
            IdlingSlotStatus.REOPENED -> return next == IdlingSlotStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: IdlingSlotStatus) {
        if (!canMoveTo(next)) {
            error("idling slot $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"idling slot"} $code"

    fun effectiveTotal(): Double = round(version.toDouble() * threshold * 100) / 100

    fun snapshot(): IdlingSlot {
        return IdlingSlot(
            code = code,
            title = title,
            visible = visible,
            threshold = threshold,
            unit = unit,
            contactEmail = contactEmail,
            version = version,
            depot = depot,
            shiftCode = shiftCode,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a idling slot in status open. */
fun newIdlingSlot(code: String, title: String): IdlingSlot {
    return IdlingSlot(
        code = code,
        title = title,
        visible = false,
        threshold = 0.0,
        unit = "",
        contactEmail = "",
        version = 0,
        depot = "",
        shiftCode = "",
        score = 0.0,
        status = IdlingSlotStatus.OPEN,
    )
}
