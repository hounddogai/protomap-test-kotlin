package com.acme.transport.lastmile.locker.lockerquota

import com.acme.transport.kit.text.normalize

/** Where a strict locker threshold is in its life cycle. */
enum class StrictLockerThresholdStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict locker threshold as the locker team keeps it. */
data class StrictLockerThreshold(
    var code: String,
    var name: String,
    var externalRef: String,
    var weight: Double,
    var vehicleId: String,
    var country: String,
    var phone: String,
    var status: StrictLockerThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictLockerThresholdStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictLockerThresholdStatus): Boolean {
        when (status) {
            StrictLockerThresholdStatus.OPEN -> return next == StrictLockerThresholdStatus.RESOLVED
            StrictLockerThresholdStatus.IN_REVIEW -> {
                when (next) {
                    StrictLockerThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictLockerThresholdStatus.RESOLVED -> return next == StrictLockerThresholdStatus.ARCHIVED
            StrictLockerThresholdStatus.REOPENED -> return next == StrictLockerThresholdStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: StrictLockerThresholdStatus) {
        if (!canMoveTo(next)) {
            error("strict locker threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = weight

    fun snapshot(): StrictLockerThreshold {
        return StrictLockerThreshold(
            code = code,
            name = name,
            externalRef = externalRef,
            weight = weight,
            vehicleId = vehicleId,
            country = country,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a strict locker threshold in status open. */
fun newStrictLockerThreshold(code: String, name: String): StrictLockerThreshold {
    return StrictLockerThreshold(
        code = code,
        name = name,
        externalRef = "",
        weight = 0.0,
        vehicleId = "",
        country = "",
        phone = "",
        status = StrictLockerThresholdStatus.OPEN,
    )
}
