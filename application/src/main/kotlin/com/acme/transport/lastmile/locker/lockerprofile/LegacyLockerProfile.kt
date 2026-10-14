package com.acme.transport.lastmile.locker.lockerprofile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy locker profile records of lastmile's locker work, and the rules, stores, and calculations around
// them.

/** Where a legacy locker profile is in its life cycle. */
enum class LegacyLockerProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A legacy locker profile. Its code is unique within a tenant. */
data class LegacyLockerProfile(
    var code: String,
    var name: String,
    var retries: Int,
    var pallets: Int,
    var driverId: String,
    var contactEmail: String,
    var visible: Boolean,
    var updatedAt: Int,
    var discount: Double,
    var version: Int,
    var status: LegacyLockerProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyLockerProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyLockerProfileStatus): Boolean {
        when (status) {
            LegacyLockerProfileStatus.DRAFT -> return next == LegacyLockerProfileStatus.ACTIVE
            LegacyLockerProfileStatus.PENDING -> return next == LegacyLockerProfileStatus.ACTIVE || next == LegacyLockerProfileStatus.SUSPENDED
            LegacyLockerProfileStatus.ACTIVE -> {
                when (next) {
                    LegacyLockerProfileStatus.SUSPENDED, LegacyLockerProfileStatus.CLOSED -> return true
                }
                return false
            }
            LegacyLockerProfileStatus.SUSPENDED -> {
                when (next) {
                    LegacyLockerProfileStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyLockerProfileStatus) {
        if (!canMoveTo(next)) {
            error("legacy locker profile $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round((updatedAt.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): LegacyLockerProfile {
        return LegacyLockerProfile(
            code = code,
            name = name,
            retries = retries,
            pallets = pallets,
            driverId = driverId,
            contactEmail = contactEmail,
            visible = visible,
            updatedAt = updatedAt,
            discount = discount,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy locker profile in status draft. */
fun newLegacyLockerProfile(code: String, name: String): LegacyLockerProfile {
    return LegacyLockerProfile(
        code = code,
        name = name,
        retries = 0,
        pallets = 0,
        driverId = "",
        contactEmail = "",
        visible = false,
        updatedAt = 0,
        discount = 0.0,
        version = 0,
        status = LegacyLockerProfileStatus.DRAFT,
    )
}
