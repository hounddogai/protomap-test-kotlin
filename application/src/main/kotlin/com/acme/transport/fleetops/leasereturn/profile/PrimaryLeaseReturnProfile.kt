package com.acme.transport.fleetops.leasereturn.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary lease return profile records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a primary lease return profile is in its life cycle. */
enum class PrimaryLeaseReturnProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a primary lease return profile and its current status. */
data class PrimaryLeaseReturnProfile(
    var code: String,
    var name: String,
    var attempts: Int,
    var quantity: Int,
    var carrier: String,
    var startsAt: Int,
    var score: Double,
    var status: PrimaryLeaseReturnProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryLeaseReturnProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryLeaseReturnProfileStatus): Boolean {
        when (status) {
            PrimaryLeaseReturnProfileStatus.PLANNED -> return next == PrimaryLeaseReturnProfileStatus.SCHEDULED
            PrimaryLeaseReturnProfileStatus.SCHEDULED -> return next == PrimaryLeaseReturnProfileStatus.COMPLETED || next == PrimaryLeaseReturnProfileStatus.CANCELLED
            PrimaryLeaseReturnProfileStatus.IN_PROGRESS -> {
                when (next) {
                    PrimaryLeaseReturnProfileStatus.COMPLETED -> return true
                }
                return false
            }
            PrimaryLeaseReturnProfileStatus.COMPLETED -> return next == PrimaryLeaseReturnProfileStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PrimaryLeaseReturnProfileStatus) {
        if (!canMoveTo(next)) {
            error("primary lease return profile $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun projectedTotal(): Double = round((score - startsAt.toDouble()) * 100) / 100

    fun snapshot(): PrimaryLeaseReturnProfile {
        return PrimaryLeaseReturnProfile(
            code = code,
            name = name,
            attempts = attempts,
            quantity = quantity,
            carrier = carrier,
            startsAt = startsAt,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary lease return profile in status planned. */
fun newPrimaryLeaseReturnProfile(code: String, name: String): PrimaryLeaseReturnProfile {
    return PrimaryLeaseReturnProfile(
        code = code,
        name = name,
        attempts = 0,
        quantity = 0,
        carrier = "",
        startsAt = 0,
        score = 0.0,
        status = PrimaryLeaseReturnProfileStatus.PLANNED,
    )
}
