package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling attempt tier is in its life cycle. */
enum class RollingAttemptTierStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rolling attempt tier as the attempt team keeps it. */
data class RollingAttemptTier(
    var code: String,
    var label: String,
    var country: String,
    var locale: String,
    var archived: Boolean,
    var fuelLiters: Double,
    var carrier: String,
    var driverId: String,
    var lane: String,
    var amount: Double,
    var attempts: Int,
    var status: RollingAttemptTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAttemptTierStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAttemptTierStatus): Boolean {
        when (status) {
            RollingAttemptTierStatus.DRAFT -> return next == RollingAttemptTierStatus.PENDING || next == RollingAttemptTierStatus.SUSPENDED
            RollingAttemptTierStatus.PENDING -> {
                when (next) {
                    RollingAttemptTierStatus.SUSPENDED -> return true
                }
                return false
            }
            RollingAttemptTierStatus.ACTIVE -> return next == RollingAttemptTierStatus.CLOSED
            RollingAttemptTierStatus.SUSPENDED -> return next == RollingAttemptTierStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RollingAttemptTierStatus) {
        if (!canMoveTo(next)) {
            error("rolling attempt tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedValue(): Double = round((amount - attempts.toDouble()) * 100) / 100

    fun snapshot(): RollingAttemptTier {
        return RollingAttemptTier(
            code = code,
            label = label,
            country = country,
            locale = locale,
            archived = archived,
            fuelLiters = fuelLiters,
            carrier = carrier,
            driverId = driverId,
            lane = lane,
            amount = amount,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a rolling attempt tier in status draft. */
fun newRollingAttemptTier(code: String, label: String): RollingAttemptTier {
    return RollingAttemptTier(
        code = code,
        label = label,
        country = "",
        locale = "",
        archived = false,
        fuelLiters = 0.0,
        carrier = "",
        driverId = "",
        lane = "",
        amount = 0.0,
        attempts = 0,
        status = RollingAttemptTierStatus.DRAFT,
    )
}
