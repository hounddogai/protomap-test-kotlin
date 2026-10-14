package com.acme.transport.terminal.appointment.regionalappointmentmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment tier is in its life cycle. */
enum class AppointmentTierStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One appointment tier, with what the planners need to act on it. */
data class AppointmentTier(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var contactEmail: String,
    var floorValue: Double,
    var depot: String,
    var loadKg: Double,
    var status: AppointmentTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentTierStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentTierStatus): Boolean {
        when (status) {
            AppointmentTierStatus.CREATED -> {
                when (next) {
                    AppointmentTierStatus.VALIDATED, AppointmentTierStatus.PUBLISHED -> return true
                }
                return false
            }
            AppointmentTierStatus.VALIDATED -> return next == AppointmentTierStatus.PUBLISHED || next == AppointmentTierStatus.RETIRED
            AppointmentTierStatus.PUBLISHED -> {
                when (next) {
                    AppointmentTierStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentTierStatus) {
        if (!canMoveTo(next)) {
            error("appointment tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"appointment tier"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netLoad(): Double = round((floorValue + loadKg) * 100) / 100

    fun snapshot(): AppointmentTier {
        return AppointmentTier(
            code = code,
            label = label,
            tags = tags,
            contactEmail = contactEmail,
            floorValue = floorValue,
            depot = depot,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a appointment tier in status created. */
fun newAppointmentTier(code: String, label: String): AppointmentTier {
    return AppointmentTier(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        contactEmail = "",
        floorValue = 0.0,
        depot = "",
        loadKg = 0.0,
        status = AppointmentTierStatus.CREATED,
    )
}
