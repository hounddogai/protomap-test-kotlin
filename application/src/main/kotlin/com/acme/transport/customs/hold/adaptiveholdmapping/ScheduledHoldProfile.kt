package com.acme.transport.customs.hold.adaptiveholdmapping

import com.acme.transport.kit.text.normalize

/** Where a scheduled hold profile is in its life cycle. */
enum class ScheduledHoldProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A scheduled hold profile. Its code is unique within a tenant. */
data class ScheduledHoldProfile(
    var code: String,
    var name: String,
    var contactEmail: String,
    var region: String,
    var odometer: Int,
    var status: ScheduledHoldProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledHoldProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledHoldProfileStatus): Boolean {
        when (status) {
            ScheduledHoldProfileStatus.CREATED -> {
                when (next) {
                    ScheduledHoldProfileStatus.VALIDATED, ScheduledHoldProfileStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledHoldProfileStatus.VALIDATED -> return next == ScheduledHoldProfileStatus.RETIRED
            ScheduledHoldProfileStatus.PUBLISHED -> return next == ScheduledHoldProfileStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledHoldProfileStatus) {
        if (!canMoveTo(next)) {
            error("scheduled hold profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): ScheduledHoldProfile {
        return ScheduledHoldProfile(
            code = code,
            name = name,
            contactEmail = contactEmail,
            region = region,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled hold profile in status created. */
fun newScheduledHoldProfile(code: String, name: String): ScheduledHoldProfile {
    return ScheduledHoldProfile(
        code = code,
        name = name,
        contactEmail = "",
        region = "",
        odometer = 0,
        status = ScheduledHoldProfileStatus.CREATED,
    )
}
