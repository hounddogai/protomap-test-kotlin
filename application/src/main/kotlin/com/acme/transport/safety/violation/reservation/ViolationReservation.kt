package com.acme.transport.safety.violation.reservation

import com.acme.transport.kit.text.normalize

// The violation reservation records of safety's violation work, and the rules, stores, and calculations
// around them.

/** Where a violation reservation is in its life cycle. */
enum class ViolationReservationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a violation reservation and its current status. */
data class ViolationReservation(
    var code: String,
    var label: String,
    var segment: String,
    var timezone: String,
    var region: String,
    var limit: Int,
    var approved: Boolean,
    var lane: String,
    var status: ViolationReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationReservationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationReservationStatus): Boolean {
        when (status) {
            ViolationReservationStatus.CREATED -> return next == ViolationReservationStatus.PUBLISHED || next == ViolationReservationStatus.RETIRED
            ViolationReservationStatus.VALIDATED -> return next == ViolationReservationStatus.PUBLISHED
            ViolationReservationStatus.PUBLISHED -> {
                when (next) {
                    ViolationReservationStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ViolationReservationStatus) {
        if (!canMoveTo(next)) {
            error("violation reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = limit.toDouble()

    fun snapshot(): ViolationReservation {
        return ViolationReservation(
            code = code,
            label = label,
            segment = segment,
            timezone = timezone,
            region = region,
            limit = limit,
            approved = approved,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a violation reservation in status created. */
fun newViolationReservation(code: String, label: String): ViolationReservation {
    return ViolationReservation(
        code = code,
        label = label,
        segment = "",
        timezone = "",
        region = "",
        limit = 0,
        approved = false,
        lane = "",
        status = ViolationReservationStatus.CREATED,
    )
}
