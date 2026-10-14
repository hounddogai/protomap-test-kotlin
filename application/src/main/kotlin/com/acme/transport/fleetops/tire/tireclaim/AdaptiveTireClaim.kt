package com.acme.transport.fleetops.tire.tireclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive tire claim records of fleetops's tire work, and the rules, stores, and calculations around
// them.

/** Where a adaptive tire claim is in its life cycle. */
enum class AdaptiveTireClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One adaptive tire claim, with what the planners need to act on it. */
data class AdaptiveTireClaim(
    var code: String,
    var title: String,
    var dock: String,
    var currency: String,
    var fuelLiters: Double,
    var stopCount: Int,
    var tags: MutableList<String>,
    var contactEmail: String,
    var status: AdaptiveTireClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTireClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTireClaimStatus): Boolean {
        when (status) {
            AdaptiveTireClaimStatus.CREATED -> return next == AdaptiveTireClaimStatus.VALIDATED
            AdaptiveTireClaimStatus.VALIDATED -> return next == AdaptiveTireClaimStatus.PUBLISHED
            AdaptiveTireClaimStatus.PUBLISHED -> return next == AdaptiveTireClaimStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveTireClaimStatus) {
        if (!canMoveTo(next)) {
            error("adaptive tire claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedExposure(): Double = round((fuelLiters - stopCount.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveTireClaim {
        return AdaptiveTireClaim(
            code = code,
            title = title,
            dock = dock,
            currency = currency,
            fuelLiters = fuelLiters,
            stopCount = stopCount,
            tags = tags,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive tire claim in status created. */
fun newAdaptiveTireClaim(code: String, title: String): AdaptiveTireClaim {
    return AdaptiveTireClaim(
        code = code,
        title = title,
        dock = "",
        currency = "",
        fuelLiters = 0.0,
        stopCount = 0,
        tags = mutableListOf<String>(),
        contactEmail = "",
        status = AdaptiveTireClaimStatus.CREATED,
    )
}
