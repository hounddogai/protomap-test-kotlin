package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional availability digest records of crewing's availability work, and the rules, stores, and
// calculations around them.

/** Where a regional availability digest is in its life cycle. */
enum class RegionalAvailabilityDigestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One regional availability digest, with what the planners need to act on it. */
data class RegionalAvailabilityDigest(
    var code: String,
    var label: String,
    var notes: String,
    var attempts: Int,
    var approved: Boolean,
    var sequence: Int,
    var summary: String,
    var amount: Double,
    var status: RegionalAvailabilityDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalAvailabilityDigestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalAvailabilityDigestStatus): Boolean {
        when (status) {
            RegionalAvailabilityDigestStatus.CREATED -> return next == RegionalAvailabilityDigestStatus.RETIRED
            RegionalAvailabilityDigestStatus.VALIDATED -> {
                when (next) {
                    RegionalAvailabilityDigestStatus.PUBLISHED, RegionalAvailabilityDigestStatus.RETIRED -> return true
                }
                return false
            }
            RegionalAvailabilityDigestStatus.PUBLISHED -> return next == RegionalAvailabilityDigestStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalAvailabilityDigestStatus) {
        if (!canMoveTo(next)) {
            error("regional availability digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"regional availability digest"} $code"

    fun adjustedLoad(): Double = round((attempts.toDouble() - amount) * 100) / 100

    fun snapshot(): RegionalAvailabilityDigest {
        return RegionalAvailabilityDigest(
            code = code,
            label = label,
            notes = notes,
            attempts = attempts,
            approved = approved,
            sequence = sequence,
            summary = summary,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional availability digest in status created. */
fun newRegionalAvailabilityDigest(code: String, label: String): RegionalAvailabilityDigest {
    return RegionalAvailabilityDigest(
        code = code,
        label = label,
        notes = "",
        attempts = 0,
        approved = false,
        sequence = 0,
        summary = "",
        amount = 0.0,
        status = RegionalAvailabilityDigestStatus.CREATED,
    )
}
