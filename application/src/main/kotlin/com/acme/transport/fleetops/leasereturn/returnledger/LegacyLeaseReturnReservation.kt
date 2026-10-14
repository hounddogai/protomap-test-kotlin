package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy lease return reservation is in its life cycle. */
enum class LegacyLeaseReturnReservationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A legacy lease return reservation. Its code is unique within a tenant. */
data class LegacyLeaseReturnReservation(
    var code: String,
    var name: String,
    var margin: Double,
    var startsAt: Int,
    var ratio: Double,
    var contactEmail: String,
    var checksum: String,
    var locale: String,
    var floorValue: Double,
    var country: String,
    var status: LegacyLeaseReturnReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyLeaseReturnReservationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyLeaseReturnReservationStatus): Boolean {
        when (status) {
            LegacyLeaseReturnReservationStatus.OPEN -> return next == LegacyLeaseReturnReservationStatus.RESOLVED
            LegacyLeaseReturnReservationStatus.IN_REVIEW -> return next == LegacyLeaseReturnReservationStatus.RESOLVED || next == LegacyLeaseReturnReservationStatus.REOPENED
            LegacyLeaseReturnReservationStatus.RESOLVED -> {
                when (next) {
                    LegacyLeaseReturnReservationStatus.REOPENED -> return true
                }
                return false
            }
            LegacyLeaseReturnReservationStatus.REOPENED -> {
                when (next) {
                    LegacyLeaseReturnReservationStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyLeaseReturnReservationStatus) {
        if (!canMoveTo(next)) {
            error("legacy lease return reservation $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveValue(): Double = round((floorValue + ratio) * 100) / 100

    fun snapshot(): LegacyLeaseReturnReservation {
        return LegacyLeaseReturnReservation(
            code = code,
            name = name,
            margin = margin,
            startsAt = startsAt,
            ratio = ratio,
            contactEmail = contactEmail,
            checksum = checksum,
            locale = locale,
            floorValue = floorValue,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a legacy lease return reservation in status open. */
fun newLegacyLeaseReturnReservation(code: String, name: String): LegacyLeaseReturnReservation {
    return LegacyLeaseReturnReservation(
        code = code,
        name = name,
        margin = 0.0,
        startsAt = 0,
        ratio = 0.0,
        contactEmail = "",
        checksum = "",
        locale = "",
        floorValue = 0.0,
        country = "",
        status = LegacyLeaseReturnReservationStatus.OPEN,
    )
}
