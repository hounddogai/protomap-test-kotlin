package com.acme.transport.safety.violation.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling violation profile records of safety's violation work, and the rules, stores, and calculations
// around them.

/** Where a rolling violation profile is in its life cycle. */
enum class RollingViolationProfileStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a rolling violation profile and its current status. */
data class RollingViolationProfile(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var approved: Boolean,
    var archived: Boolean,
    var carrier: String,
    var fuelLiters: Double,
    var ceiling: Double,
    var discount: Double,
    var loadKg: Double,
    var status: RollingViolationProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingViolationProfileStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingViolationProfileStatus): Boolean {
        when (status) {
            RollingViolationProfileStatus.PROPOSED -> {
                when (next) {
                    RollingViolationProfileStatus.REJECTED, RollingViolationProfileStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingViolationProfileStatus.APPROVED -> return next == RollingViolationProfileStatus.REJECTED
            RollingViolationProfileStatus.REJECTED -> return next == RollingViolationProfileStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RollingViolationProfileStatus) {
        if (!canMoveTo(next)) {
            error("rolling violation profile $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedValue(): Double = round(fuelLiters * discount * 100) / 100

    fun snapshot(): RollingViolationProfile {
        return RollingViolationProfile(
            code = code,
            title = title,
            expiresAt = expiresAt,
            approved = approved,
            archived = archived,
            carrier = carrier,
            fuelLiters = fuelLiters,
            ceiling = ceiling,
            discount = discount,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling violation profile in status proposed. */
fun newRollingViolationProfile(code: String, title: String): RollingViolationProfile {
    return RollingViolationProfile(
        code = code,
        title = title,
        expiresAt = 0,
        approved = false,
        archived = false,
        carrier = "",
        fuelLiters = 0.0,
        ceiling = 0.0,
        discount = 0.0,
        loadKg = 0.0,
        status = RollingViolationProfileStatus.PROPOSED,
    )
}
