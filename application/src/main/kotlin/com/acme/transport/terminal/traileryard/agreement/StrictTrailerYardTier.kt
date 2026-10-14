package com.acme.transport.terminal.traileryard.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict trailer yard tier is in its life cycle. */
enum class StrictTrailerYardTierStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One strict trailer yard tier, with what the planners need to act on it. */
data class StrictTrailerYardTier(
    var code: String,
    var label: String,
    var minimum: Double,
    var unit: String,
    var amount: Double,
    var checksum: String,
    var contactEmail: String,
    var status: StrictTrailerYardTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTrailerYardTierStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTrailerYardTierStatus): Boolean {
        when (status) {
            StrictTrailerYardTierStatus.PROPOSED -> {
                when (next) {
                    StrictTrailerYardTierStatus.APPROVED, StrictTrailerYardTierStatus.REJECTED -> return true
                }
                return false
            }
            StrictTrailerYardTierStatus.APPROVED -> return next == StrictTrailerYardTierStatus.REJECTED || next == StrictTrailerYardTierStatus.WITHDRAWN
            StrictTrailerYardTierStatus.REJECTED -> {
                when (next) {
                    StrictTrailerYardTierStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictTrailerYardTierStatus) {
        if (!canMoveTo(next)) {
            error("strict trailer yard tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveLoad(): Double = round((amount - minimum) * 100) / 100

    fun snapshot(): StrictTrailerYardTier {
        return StrictTrailerYardTier(
            code = code,
            label = label,
            minimum = minimum,
            unit = unit,
            amount = amount,
            checksum = checksum,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a strict trailer yard tier in status proposed. */
fun newStrictTrailerYardTier(code: String, label: String): StrictTrailerYardTier {
    return StrictTrailerYardTier(
        code = code,
        label = label,
        minimum = 0.0,
        unit = "",
        amount = 0.0,
        checksum = "",
        contactEmail = "",
        status = StrictTrailerYardTierStatus.PROPOSED,
    )
}
