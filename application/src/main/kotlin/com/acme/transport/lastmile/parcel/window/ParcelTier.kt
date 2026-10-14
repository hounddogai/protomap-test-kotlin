package com.acme.transport.lastmile.parcel.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel tier is in its life cycle. */
enum class ParcelTierStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a parcel tier and its current status. */
data class ParcelTier(
    var code: String,
    var name: String,
    var version: Int,
    var odometer: Int,
    var contactEmail: String,
    var approved: Boolean,
    var status: ParcelTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelTierStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelTierStatus): Boolean {
        when (status) {
            ParcelTierStatus.PROPOSED -> return next == ParcelTierStatus.WITHDRAWN
            ParcelTierStatus.APPROVED -> {
                when (next) {
                    ParcelTierStatus.REJECTED, ParcelTierStatus.WITHDRAWN -> return true
                }
                return false
            }
            ParcelTierStatus.REJECTED -> {
                when (next) {
                    ParcelTierStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelTierStatus) {
        if (!canMoveTo(next)) {
            error("parcel tier $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun effectiveExposure(): Double = round((odometer.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): ParcelTier {
        return ParcelTier(
            code = code,
            name = name,
            version = version,
            odometer = odometer,
            contactEmail = contactEmail,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a parcel tier in status proposed. */
fun newParcelTier(code: String, name: String): ParcelTier {
    return ParcelTier(
        code = code,
        name = name,
        version = 0,
        odometer = 0,
        contactEmail = "",
        approved = false,
        status = ParcelTierStatus.PROPOSED,
    )
}
