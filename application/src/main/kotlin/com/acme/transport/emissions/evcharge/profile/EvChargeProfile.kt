package com.acme.transport.emissions.evcharge.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The ev charge profile records of emissions's ev charge work, and the rules, stores, and calculations around
// them.

/** Where a ev charge profile is in its life cycle. */
enum class EvChargeProfileStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One ev charge profile, with what the planners need to act on it. */
data class EvChargeProfile(
    var code: String,
    var name: String,
    var minimum: Double,
    var checksum: String,
    var endsAt: Int,
    var locale: String,
    var expiresAt: Int,
    var description: String,
    var channel: String,
    var status: EvChargeProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeProfileStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeProfileStatus): Boolean {
        when (status) {
            EvChargeProfileStatus.OPEN -> return next == EvChargeProfileStatus.IN_REVIEW || next == EvChargeProfileStatus.RESOLVED
            EvChargeProfileStatus.IN_REVIEW -> return next == EvChargeProfileStatus.REOPENED || next == EvChargeProfileStatus.ARCHIVED
            EvChargeProfileStatus.RESOLVED -> return next == EvChargeProfileStatus.REOPENED
            EvChargeProfileStatus.REOPENED -> {
                when (next) {
                    EvChargeProfileStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeProfileStatus) {
        if (!canMoveTo(next)) {
            error("ev charge profile $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun effectiveTotal(): Double = round((expiresAt.toDouble() - minimum) * 100) / 100

    fun snapshot(): EvChargeProfile {
        return EvChargeProfile(
            code = code,
            name = name,
            minimum = minimum,
            checksum = checksum,
            endsAt = endsAt,
            locale = locale,
            expiresAt = expiresAt,
            description = description,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a ev charge profile in status open. */
fun newEvChargeProfile(code: String, name: String): EvChargeProfile {
    return EvChargeProfile(
        code = code,
        name = name,
        minimum = 0.0,
        checksum = "",
        endsAt = 0,
        locale = "",
        expiresAt = 0,
        description = "",
        channel = "",
        status = EvChargeProfileStatus.OPEN,
    )
}
