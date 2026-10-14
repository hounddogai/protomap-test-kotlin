package com.acme.transport.crewing.roster.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default roster claim is in its life cycle. */
enum class DefaultRosterClaimStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A default roster claim as the roster team keeps it. */
data class DefaultRosterClaim(
    var code: String,
    var label: String,
    var retries: Int,
    var createdAt: Int,
    var distanceKm: Double,
    var externalRef: String,
    var currency: String,
    var stopCount: Int,
    var startsAt: Int,
    var status: DefaultRosterClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRosterClaimStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRosterClaimStatus): Boolean {
        when (status) {
            DefaultRosterClaimStatus.PLANNED -> {
                when (next) {
                    DefaultRosterClaimStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultRosterClaimStatus.SCHEDULED -> return next == DefaultRosterClaimStatus.IN_PROGRESS || next == DefaultRosterClaimStatus.COMPLETED
            DefaultRosterClaimStatus.IN_PROGRESS -> {
                when (next) {
                    DefaultRosterClaimStatus.COMPLETED, DefaultRosterClaimStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultRosterClaimStatus.COMPLETED -> {
                when (next) {
                    DefaultRosterClaimStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultRosterClaimStatus) {
        if (!canMoveTo(next)) {
            error("default roster claim $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedLoad(): Double = round(distanceKm * createdAt.toDouble() * 100) / 100

    fun snapshot(): DefaultRosterClaim {
        return DefaultRosterClaim(
            code = code,
            label = label,
            retries = retries,
            createdAt = createdAt,
            distanceKm = distanceKm,
            externalRef = externalRef,
            currency = currency,
            stopCount = stopCount,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default roster claim in status planned. */
fun newDefaultRosterClaim(code: String, label: String): DefaultRosterClaim {
    return DefaultRosterClaim(
        code = code,
        label = label,
        retries = 0,
        createdAt = 0,
        distanceKm = 0.0,
        externalRef = "",
        currency = "",
        stopCount = 0,
        startsAt = 0,
        status = DefaultRosterClaimStatus.PLANNED,
    )
}
