package com.acme.transport.crewing.availability.availabilityreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive availability tier is in its life cycle. */
enum class AdaptiveAvailabilityTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One adaptive availability tier, with what the planners need to act on it. */
data class AdaptiveAvailabilityTier(
    var code: String,
    var title: String,
    var sequence: Int,
    var score: Double,
    var ownerId: String,
    var retries: Int,
    var summary: String,
    var version: Int,
    var currency: String,
    var etaMinutes: Int,
    var status: AdaptiveAvailabilityTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAvailabilityTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAvailabilityTierStatus): Boolean {
        when (status) {
            AdaptiveAvailabilityTierStatus.PLANNED -> return next == AdaptiveAvailabilityTierStatus.SCHEDULED || next == AdaptiveAvailabilityTierStatus.CANCELLED
            AdaptiveAvailabilityTierStatus.SCHEDULED -> {
                when (next) {
                    AdaptiveAvailabilityTierStatus.COMPLETED -> return true
                }
                return false
            }
            AdaptiveAvailabilityTierStatus.IN_PROGRESS -> {
                when (next) {
                    AdaptiveAvailabilityTierStatus.COMPLETED, AdaptiveAvailabilityTierStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveAvailabilityTierStatus.COMPLETED -> {
                when (next) {
                    AdaptiveAvailabilityTierStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveAvailabilityTierStatus) {
        if (!canMoveTo(next)) {
            error("adaptive availability tier $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun adjustedVolume(): Double = round((version.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveAvailabilityTier {
        return AdaptiveAvailabilityTier(
            code = code,
            title = title,
            sequence = sequence,
            score = score,
            ownerId = ownerId,
            retries = retries,
            summary = summary,
            version = version,
            currency = currency,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive availability tier in status planned. */
fun newAdaptiveAvailabilityTier(code: String, title: String): AdaptiveAvailabilityTier {
    return AdaptiveAvailabilityTier(
        code = code,
        title = title,
        sequence = 0,
        score = 0.0,
        ownerId = "",
        retries = 0,
        summary = "",
        version = 0,
        currency = "",
        etaMinutes = 0,
        status = AdaptiveAvailabilityTierStatus.PLANNED,
    )
}
