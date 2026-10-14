package com.acme.transport.crewing.roster.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The roster digest records of crewing's roster work, and the rules, stores, and calculations around them.

/** Where a roster digest is in its life cycle. */
enum class RosterDigestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a roster digest and its current status. */
data class RosterDigest(
    var code: String,
    var name: String,
    var quantity: Int,
    var durationMinutes: Int,
    var distanceKm: Double,
    var status: RosterDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterDigestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterDigestStatus): Boolean {
        when (status) {
            RosterDigestStatus.CREATED -> {
                when (next) {
                    RosterDigestStatus.VALIDATED, RosterDigestStatus.PUBLISHED -> return true
                }
                return false
            }
            RosterDigestStatus.VALIDATED -> return next == RosterDigestStatus.PUBLISHED
            RosterDigestStatus.PUBLISHED -> return next == RosterDigestStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RosterDigestStatus) {
        if (!canMoveTo(next)) {
            error("roster digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveValue(): Double = round((durationMinutes.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): RosterDigest {
        return RosterDigest(
            code = code,
            name = name,
            quantity = quantity,
            durationMinutes = durationMinutes,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a roster digest in status created. */
fun newRosterDigest(code: String, name: String): RosterDigest {
    return RosterDigest(
        code = code,
        name = name,
        quantity = 0,
        durationMinutes = 0,
        distanceKm = 0.0,
        status = RosterDigestStatus.CREATED,
    )
}
