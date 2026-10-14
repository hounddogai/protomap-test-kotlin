package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive attempt request records of lastmile's attempt work, and the rules, stores, and calculations
// around them.

/** Where a adaptive attempt request is in its life cycle. */
enum class AdaptiveAttemptRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A adaptive attempt request as the attempt team keeps it. */
data class AdaptiveAttemptRequest(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var weight: Double,
    var distanceKm: Double,
    var reviewer: String,
    var windowDays: Int,
    var status: AdaptiveAttemptRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAttemptRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAttemptRequestStatus): Boolean {
        when (status) {
            AdaptiveAttemptRequestStatus.REQUESTED -> return next == AdaptiveAttemptRequestStatus.GRANTED || next == AdaptiveAttemptRequestStatus.EXPIRED
            AdaptiveAttemptRequestStatus.GRANTED -> {
                when (next) {
                    AdaptiveAttemptRequestStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveAttemptRequestStatus.REVOKED -> {
                when (next) {
                    AdaptiveAttemptRequestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveAttemptRequestStatus) {
        if (!canMoveTo(next)) {
            error("adaptive attempt request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedLoad(): Double = round((distanceKm + weight) * 100) / 100

    fun snapshot(): AdaptiveAttemptRequest {
        return AdaptiveAttemptRequest(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            weight = weight,
            distanceKm = distanceKm,
            reviewer = reviewer,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive attempt request in status requested. */
fun newAdaptiveAttemptRequest(code: String, label: String): AdaptiveAttemptRequest {
    return AdaptiveAttemptRequest(
        code = code,
        label = label,
        durationMinutes = 0,
        weight = 0.0,
        distanceKm = 0.0,
        reviewer = "",
        windowDays = 0,
        status = AdaptiveAttemptRequestStatus.REQUESTED,
    )
}
