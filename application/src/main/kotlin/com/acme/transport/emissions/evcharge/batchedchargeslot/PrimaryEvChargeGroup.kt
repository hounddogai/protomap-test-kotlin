package com.acme.transport.emissions.evcharge.batchedchargeslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary ev charge group is in its life cycle. */
enum class PrimaryEvChargeGroupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A primary ev charge group as the ev charge team keeps it. */
data class PrimaryEvChargeGroup(
    var code: String,
    var name: String,
    var contactEmail: String,
    var checksum: String,
    var startsAt: Int,
    var etaMinutes: Int,
    var amount: Double,
    var floorValue: Double,
    var status: PrimaryEvChargeGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryEvChargeGroupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryEvChargeGroupStatus): Boolean {
        when (status) {
            PrimaryEvChargeGroupStatus.OPEN -> {
                when (next) {
                    PrimaryEvChargeGroupStatus.IN_REVIEW -> return true
                }
                return false
            }
            PrimaryEvChargeGroupStatus.IN_REVIEW -> {
                when (next) {
                    PrimaryEvChargeGroupStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryEvChargeGroupStatus.RESOLVED -> {
                when (next) {
                    PrimaryEvChargeGroupStatus.REOPENED, PrimaryEvChargeGroupStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryEvChargeGroupStatus.REOPENED -> {
                when (next) {
                    PrimaryEvChargeGroupStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryEvChargeGroupStatus) {
        if (!canMoveTo(next)) {
            error("primary ev charge group $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveVolume(): Double = round((floorValue - amount) * 100) / 100

    fun snapshot(): PrimaryEvChargeGroup {
        return PrimaryEvChargeGroup(
            code = code,
            name = name,
            contactEmail = contactEmail,
            checksum = checksum,
            startsAt = startsAt,
            etaMinutes = etaMinutes,
            amount = amount,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary ev charge group in status open. */
fun newPrimaryEvChargeGroup(code: String, name: String): PrimaryEvChargeGroup {
    return PrimaryEvChargeGroup(
        code = code,
        name = name,
        contactEmail = "",
        checksum = "",
        startsAt = 0,
        etaMinutes = 0,
        amount = 0.0,
        floorValue = 0.0,
        status = PrimaryEvChargeGroupStatus.OPEN,
    )
}
