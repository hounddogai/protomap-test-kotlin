package com.acme.transport.lastmile.redelivery.budget

import com.acme.transport.kit.text.normalize

/** Where a scheduled redelivery preference is in its life cycle. */
enum class ScheduledRedeliveryPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a scheduled redelivery preference and its current status. */
data class ScheduledRedeliveryPreference(
    var code: String,
    var name: String,
    var driverId: String,
    var ownerId: String,
    var odometer: Int,
    var status: ScheduledRedeliveryPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRedeliveryPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRedeliveryPreferenceStatus): Boolean {
        when (status) {
            ScheduledRedeliveryPreferenceStatus.REQUESTED -> return next == ScheduledRedeliveryPreferenceStatus.GRANTED
            ScheduledRedeliveryPreferenceStatus.GRANTED -> {
                when (next) {
                    ScheduledRedeliveryPreferenceStatus.REVOKED, ScheduledRedeliveryPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
            ScheduledRedeliveryPreferenceStatus.REVOKED -> {
                when (next) {
                    ScheduledRedeliveryPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledRedeliveryPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("scheduled redelivery preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): ScheduledRedeliveryPreference {
        return ScheduledRedeliveryPreference(
            code = code,
            name = name,
            driverId = driverId,
            ownerId = ownerId,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled redelivery preference in status requested. */
fun newScheduledRedeliveryPreference(code: String, name: String): ScheduledRedeliveryPreference {
    return ScheduledRedeliveryPreference(
        code = code,
        name = name,
        driverId = "",
        ownerId = "",
        odometer = 0,
        status = ScheduledRedeliveryPreferenceStatus.REQUESTED,
    )
}
