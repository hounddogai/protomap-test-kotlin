package com.acme.transport.customs.hold.slot

import com.acme.transport.kit.text.normalize

/** Where a primary hold override is in its life cycle. */
enum class PrimaryHoldOverrideStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A primary hold override as the hold team keeps it. */
data class PrimaryHoldOverride(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var driverId: String,
    var country: String,
    var active: Boolean,
    var contactEmail: String,
    var routeCode: String,
    var segment: String,
    var status: PrimaryHoldOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryHoldOverrideStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryHoldOverrideStatus): Boolean {
        when (status) {
            PrimaryHoldOverrideStatus.CREATED -> {
                when (next) {
                    PrimaryHoldOverrideStatus.VALIDATED, PrimaryHoldOverrideStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryHoldOverrideStatus.VALIDATED -> {
                when (next) {
                    PrimaryHoldOverrideStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryHoldOverrideStatus.PUBLISHED -> return next == PrimaryHoldOverrideStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PrimaryHoldOverrideStatus) {
        if (!canMoveTo(next)) {
            error("primary hold override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary hold override"} $code"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): PrimaryHoldOverride {
        return PrimaryHoldOverride(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            driverId = driverId,
            country = country,
            active = active,
            contactEmail = contactEmail,
            routeCode = routeCode,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a primary hold override in status created. */
fun newPrimaryHoldOverride(code: String, title: String): PrimaryHoldOverride {
    return PrimaryHoldOverride(
        code = code,
        title = title,
        fuelLiters = 0.0,
        driverId = "",
        country = "",
        active = false,
        contactEmail = "",
        routeCode = "",
        segment = "",
        status = PrimaryHoldOverrideStatus.CREATED,
    )
}
