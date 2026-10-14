package com.acme.transport.lastmile.parcel.overrideitem

import com.acme.transport.kit.text.normalize

// The parcel override records of lastmile's parcel work, and the rules, stores, and calculations around them.

/** Where a parcel override is in its life cycle. */
enum class ParcelOverrideStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One parcel override, with what the planners need to act on it. */
data class ParcelOverride(
    var code: String,
    var title: String,
    var floorValue: Double,
    var active: Boolean,
    var category: String,
    var status: ParcelOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelOverrideStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelOverrideStatus): Boolean {
        when (status) {
            ParcelOverrideStatus.REQUESTED -> {
                when (next) {
                    ParcelOverrideStatus.REVOKED, ParcelOverrideStatus.EXPIRED -> return true
                }
                return false
            }
            ParcelOverrideStatus.GRANTED -> {
                when (next) {
                    ParcelOverrideStatus.REVOKED -> return true
                }
                return false
            }
            ParcelOverrideStatus.REVOKED -> {
                when (next) {
                    ParcelOverrideStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelOverrideStatus) {
        if (!canMoveTo(next)) {
            error("parcel override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"parcel override"} $code"

    fun measuredValue(): Double = floorValue

    fun snapshot(): ParcelOverride {
        return ParcelOverride(
            code = code,
            title = title,
            floorValue = floorValue,
            active = active,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a parcel override in status requested. */
fun newParcelOverride(code: String, title: String): ParcelOverride {
    return ParcelOverride(
        code = code,
        title = title,
        floorValue = 0.0,
        active = false,
        category = "",
        status = ParcelOverrideStatus.REQUESTED,
    )
}
