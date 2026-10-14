package com.acme.transport.terminal.appointment.adaptiveappointmentmapping

import com.acme.transport.kit.text.normalize

/** Where a legacy appointment override is in its life cycle. */
enum class LegacyAppointmentOverrideStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One legacy appointment override, with what the planners need to act on it. */
data class LegacyAppointmentOverride(
    var code: String,
    var name: String,
    var checksum: String,
    var approved: Boolean,
    var vehicleId: String,
    var depot: String,
    var lane: String,
    var score: Double,
    var status: LegacyAppointmentOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyAppointmentOverrideStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyAppointmentOverrideStatus): Boolean {
        when (status) {
            LegacyAppointmentOverrideStatus.CREATED -> {
                when (next) {
                    LegacyAppointmentOverrideStatus.VALIDATED, LegacyAppointmentOverrideStatus.PUBLISHED -> return true
                }
                return false
            }
            LegacyAppointmentOverrideStatus.VALIDATED -> {
                when (next) {
                    LegacyAppointmentOverrideStatus.PUBLISHED, LegacyAppointmentOverrideStatus.RETIRED -> return true
                }
                return false
            }
            LegacyAppointmentOverrideStatus.PUBLISHED -> {
                when (next) {
                    LegacyAppointmentOverrideStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyAppointmentOverrideStatus) {
        if (!canMoveTo(next)) {
            error("legacy appointment override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = score

    fun snapshot(): LegacyAppointmentOverride {
        return LegacyAppointmentOverride(
            code = code,
            name = name,
            checksum = checksum,
            approved = approved,
            vehicleId = vehicleId,
            depot = depot,
            lane = lane,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a legacy appointment override in status created. */
fun newLegacyAppointmentOverride(code: String, name: String): LegacyAppointmentOverride {
    return LegacyAppointmentOverride(
        code = code,
        name = name,
        checksum = "",
        approved = false,
        vehicleId = "",
        depot = "",
        lane = "",
        score = 0.0,
        status = LegacyAppointmentOverrideStatus.CREATED,
    )
}
