package com.acme.transport.lastmile.attempt.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The attempt quota records of lastmile's attempt work, and the rules, stores, and calculations around them.

/** Where a attempt quota is in its life cycle. */
enum class AttemptQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One attempt quota, with what the planners need to act on it. */
data class AttemptQuota(
    var code: String,
    var title: String,
    var odometer: Int,
    var unit: String,
    var weight: Double,
    var notes: String,
    var attempts: Int,
    var status: AttemptQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptQuotaStatus): Boolean {
        when (status) {
            AttemptQuotaStatus.IDLE -> {
                when (next) {
                    AttemptQuotaStatus.STOPPED -> return true
                }
                return false
            }
            AttemptQuotaStatus.WARMING -> {
                when (next) {
                    AttemptQuotaStatus.DRAINING -> return true
                }
                return false
            }
            AttemptQuotaStatus.READY -> return next == AttemptQuotaStatus.DRAINING || next == AttemptQuotaStatus.STOPPED
            AttemptQuotaStatus.DRAINING -> {
                when (next) {
                    AttemptQuotaStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptQuotaStatus) {
        if (!canMoveTo(next)) {
            error("attempt quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"attempt quota"} $code"

    fun netVolume(): Double = round((odometer.toDouble() + weight) * 100) / 100

    fun snapshot(): AttemptQuota {
        return AttemptQuota(
            code = code,
            title = title,
            odometer = odometer,
            unit = unit,
            weight = weight,
            notes = notes,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a attempt quota in status idle. */
fun newAttemptQuota(code: String, title: String): AttemptQuota {
    return AttemptQuota(
        code = code,
        title = title,
        odometer = 0,
        unit = "",
        weight = 0.0,
        notes = "",
        attempts = 0,
        status = AttemptQuotaStatus.IDLE,
    )
}
