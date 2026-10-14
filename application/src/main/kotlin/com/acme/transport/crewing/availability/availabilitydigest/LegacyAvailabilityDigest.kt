package com.acme.transport.crewing.availability.availabilitydigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy availability digest records of crewing's availability work, and the rules, stores, and
// calculations around them.

/** Where a legacy availability digest is in its life cycle. */
enum class LegacyAvailabilityDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy availability digest as the availability team keeps it. */
data class LegacyAvailabilityDigest(
    var code: String,
    var name: String,
    var carrier: String,
    var contactEmail: String,
    var sequence: Int,
    var depot: String,
    var driverId: String,
    var title: String,
    var archived: Boolean,
    var weight: Double,
    var ownerId: String,
    var status: LegacyAvailabilityDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyAvailabilityDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyAvailabilityDigestStatus): Boolean {
        when (status) {
            LegacyAvailabilityDigestStatus.NEW -> {
                when (next) {
                    LegacyAvailabilityDigestStatus.FAILED -> return true
                }
                return false
            }
            LegacyAvailabilityDigestStatus.QUEUED -> return next == LegacyAvailabilityDigestStatus.RUNNING || next == LegacyAvailabilityDigestStatus.DONE
            LegacyAvailabilityDigestStatus.RUNNING -> return next == LegacyAvailabilityDigestStatus.DONE || next == LegacyAvailabilityDigestStatus.FAILED
            LegacyAvailabilityDigestStatus.DONE -> {
                when (next) {
                    LegacyAvailabilityDigestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyAvailabilityDigestStatus) {
        if (!canMoveTo(next)) {
            error("legacy availability digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedTotal(): Double = round((sequence.toDouble() - weight) * 100) / 100

    fun snapshot(): LegacyAvailabilityDigest {
        return LegacyAvailabilityDigest(
            code = code,
            name = name,
            carrier = carrier,
            contactEmail = contactEmail,
            sequence = sequence,
            depot = depot,
            driverId = driverId,
            title = title,
            archived = archived,
            weight = weight,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a legacy availability digest in status new. */
fun newLegacyAvailabilityDigest(code: String, name: String): LegacyAvailabilityDigest {
    return LegacyAvailabilityDigest(
        code = code,
        name = name,
        carrier = "",
        contactEmail = "",
        sequence = 0,
        depot = "",
        driverId = "",
        title = "",
        archived = false,
        weight = 0.0,
        ownerId = "",
        status = LegacyAvailabilityDigestStatus.NEW,
    )
}
