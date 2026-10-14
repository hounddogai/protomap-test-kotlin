package com.acme.transport.safety.coaching.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling coaching preference is in its life cycle. */
enum class RollingCoachingPreferenceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rolling coaching preference as the coaching team keeps it. */
data class RollingCoachingPreference(
    var code: String,
    var title: String,
    var limit: Int,
    var discount: Double,
    var retries: Int,
    var fuelLiters: Double,
    var status: RollingCoachingPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCoachingPreferenceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCoachingPreferenceStatus): Boolean {
        when (status) {
            RollingCoachingPreferenceStatus.IDLE -> {
                when (next) {
                    RollingCoachingPreferenceStatus.WARMING, RollingCoachingPreferenceStatus.STOPPED -> return true
                }
                return false
            }
            RollingCoachingPreferenceStatus.WARMING -> return next == RollingCoachingPreferenceStatus.READY || next == RollingCoachingPreferenceStatus.STOPPED
            RollingCoachingPreferenceStatus.READY -> {
                when (next) {
                    RollingCoachingPreferenceStatus.DRAINING, RollingCoachingPreferenceStatus.STOPPED -> return true
                }
                return false
            }
            RollingCoachingPreferenceStatus.DRAINING -> {
                when (next) {
                    RollingCoachingPreferenceStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingCoachingPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("rolling coaching preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveExposure(): Double = round((limit.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): RollingCoachingPreference {
        return RollingCoachingPreference(
            code = code,
            title = title,
            limit = limit,
            discount = discount,
            retries = retries,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling coaching preference in status idle. */
fun newRollingCoachingPreference(code: String, title: String): RollingCoachingPreference {
    return RollingCoachingPreference(
        code = code,
        title = title,
        limit = 0,
        discount = 0.0,
        retries = 0,
        fuelLiters = 0.0,
        status = RollingCoachingPreferenceStatus.IDLE,
    )
}
