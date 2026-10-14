package com.acme.transport.terminal.crossdock.dockclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled cross dock claim records of terminal's cross dock work, and the rules, stores, and
// calculations around them.

/** Where a scheduled cross dock claim is in its life cycle. */
enum class ScheduledCrossDockClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One scheduled cross dock claim, with what the planners need to act on it. */
data class ScheduledCrossDockClaim(
    var code: String,
    var name: String,
    var phone: String,
    var cost: Double,
    var attributes: MutableMap<String, String>,
    var segment: String,
    var minimum: Double,
    var driverId: String,
    var pinned: Boolean,
    var currency: String,
    var status: ScheduledCrossDockClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCrossDockClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCrossDockClaimStatus): Boolean {
        when (status) {
            ScheduledCrossDockClaimStatus.CREATED -> return next == ScheduledCrossDockClaimStatus.VALIDATED || next == ScheduledCrossDockClaimStatus.PUBLISHED
            ScheduledCrossDockClaimStatus.VALIDATED -> return next == ScheduledCrossDockClaimStatus.PUBLISHED
            ScheduledCrossDockClaimStatus.PUBLISHED -> return next == ScheduledCrossDockClaimStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledCrossDockClaimStatus) {
        if (!canMoveTo(next)) {
            error("scheduled cross dock claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round((minimum - cost) * 100) / 100

    fun snapshot(): ScheduledCrossDockClaim {
        return ScheduledCrossDockClaim(
            code = code,
            name = name,
            phone = phone,
            cost = cost,
            attributes = attributes,
            segment = segment,
            minimum = minimum,
            driverId = driverId,
            pinned = pinned,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a scheduled cross dock claim in status created. */
fun newScheduledCrossDockClaim(code: String, name: String): ScheduledCrossDockClaim {
    return ScheduledCrossDockClaim(
        code = code,
        name = name,
        phone = "",
        cost = 0.0,
        attributes = mutableMapOf<String, String>(),
        segment = "",
        minimum = 0.0,
        driverId = "",
        pinned = false,
        currency = "",
        status = ScheduledCrossDockClaimStatus.CREATED,
    )
}
