package com.acme.transport.network.transittime.timeoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary transit time override records of network's transit time work, and the rules, stores, and
// calculations around them.

/** Where a primary transit time override is in its life cycle. */
enum class PrimaryTransitTimeOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a primary transit time override and its current status. */
data class PrimaryTransitTimeOverride(
    var code: String,
    var title: String,
    var minimum: Double,
    var expiresAt: Int,
    var fuelLiters: Double,
    var ownerId: String,
    var threshold: Double,
    var discount: Double,
    var status: PrimaryTransitTimeOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTransitTimeOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTransitTimeOverrideStatus): Boolean {
        when (status) {
            PrimaryTransitTimeOverrideStatus.OPEN -> {
                when (next) {
                    PrimaryTransitTimeOverrideStatus.IN_REVIEW -> return true
                }
                return false
            }
            PrimaryTransitTimeOverrideStatus.IN_REVIEW -> return next == PrimaryTransitTimeOverrideStatus.REOPENED || next == PrimaryTransitTimeOverrideStatus.ARCHIVED
            PrimaryTransitTimeOverrideStatus.RESOLVED -> return next == PrimaryTransitTimeOverrideStatus.REOPENED || next == PrimaryTransitTimeOverrideStatus.ARCHIVED
            PrimaryTransitTimeOverrideStatus.REOPENED -> {
                when (next) {
                    PrimaryTransitTimeOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryTransitTimeOverrideStatus) {
        if (!canMoveTo(next)) {
            error("primary transit time override $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"primary transit time override"} $code"

    fun weightedVolume(): Double = round(minimum * discount * 100) / 100

    fun snapshot(): PrimaryTransitTimeOverride {
        return PrimaryTransitTimeOverride(
            code = code,
            title = title,
            minimum = minimum,
            expiresAt = expiresAt,
            fuelLiters = fuelLiters,
            ownerId = ownerId,
            threshold = threshold,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary transit time override in status open. */
fun newPrimaryTransitTimeOverride(code: String, title: String): PrimaryTransitTimeOverride {
    return PrimaryTransitTimeOverride(
        code = code,
        title = title,
        minimum = 0.0,
        expiresAt = 0,
        fuelLiters = 0.0,
        ownerId = "",
        threshold = 0.0,
        discount = 0.0,
        status = PrimaryTransitTimeOverrideStatus.OPEN,
    )
}
