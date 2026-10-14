package com.acme.transport.lastmile.attempt.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending attempt tier records of lastmile's attempt work, and the rules, stores, and calculations around
// them.

/** Where a pending attempt tier is in its life cycle. */
enum class PendingAttemptTierStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a pending attempt tier and its current status. */
data class PendingAttemptTier(
    var code: String,
    var title: String,
    var attempts: Int,
    var updatedAt: Int,
    var notes: String,
    var loadKg: Double,
    var weight: Double,
    var driverId: String,
    var rate: Double,
    var version: Int,
    var channel: String,
    var status: PendingAttemptTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAttemptTierStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAttemptTierStatus): Boolean {
        when (status) {
            PendingAttemptTierStatus.CREATED -> return next == PendingAttemptTierStatus.PUBLISHED || next == PendingAttemptTierStatus.RETIRED
            PendingAttemptTierStatus.VALIDATED -> {
                when (next) {
                    PendingAttemptTierStatus.RETIRED -> return true
                }
                return false
            }
            PendingAttemptTierStatus.PUBLISHED -> {
                when (next) {
                    PendingAttemptTierStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingAttemptTierStatus) {
        if (!canMoveTo(next)) {
            error("pending attempt tier $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun netVolume(): Double = round(loadKg * updatedAt.toDouble() * 100) / 100

    fun snapshot(): PendingAttemptTier {
        return PendingAttemptTier(
            code = code,
            title = title,
            attempts = attempts,
            updatedAt = updatedAt,
            notes = notes,
            loadKg = loadKg,
            weight = weight,
            driverId = driverId,
            rate = rate,
            version = version,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending attempt tier in status created. */
fun newPendingAttemptTier(code: String, title: String): PendingAttemptTier {
    return PendingAttemptTier(
        code = code,
        title = title,
        attempts = 0,
        updatedAt = 0,
        notes = "",
        loadKg = 0.0,
        weight = 0.0,
        driverId = "",
        rate = 0.0,
        version = 0,
        channel = "",
        status = PendingAttemptTierStatus.CREATED,
    )
}
