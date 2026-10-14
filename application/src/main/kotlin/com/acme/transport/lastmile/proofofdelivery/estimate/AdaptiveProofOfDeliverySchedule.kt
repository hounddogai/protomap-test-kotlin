package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive proof of delivery schedule is in its life cycle. */
enum class AdaptiveProofOfDeliveryScheduleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a adaptive proof of delivery schedule and its current status. */
data class AdaptiveProofOfDeliverySchedule(
    var code: String,
    var name: String,
    var attempts: Int,
    var summary: String,
    var durationMinutes: Int,
    var checksum: String,
    var approved: Boolean,
    var reviewer: String,
    var status: AdaptiveProofOfDeliveryScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveProofOfDeliveryScheduleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveProofOfDeliveryScheduleStatus): Boolean {
        when (status) {
            AdaptiveProofOfDeliveryScheduleStatus.PLANNED -> {
                when (next) {
                    AdaptiveProofOfDeliveryScheduleStatus.SCHEDULED, AdaptiveProofOfDeliveryScheduleStatus.IN_PROGRESS -> return true
                }
                return false
            }
            AdaptiveProofOfDeliveryScheduleStatus.SCHEDULED -> {
                when (next) {
                    AdaptiveProofOfDeliveryScheduleStatus.COMPLETED -> return true
                }
                return false
            }
            AdaptiveProofOfDeliveryScheduleStatus.IN_PROGRESS -> return next == AdaptiveProofOfDeliveryScheduleStatus.COMPLETED
            AdaptiveProofOfDeliveryScheduleStatus.COMPLETED -> return next == AdaptiveProofOfDeliveryScheduleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AdaptiveProofOfDeliveryScheduleStatus) {
        if (!canMoveTo(next)) {
            error("adaptive proof of delivery schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedExposure(): Double = round((durationMinutes.toDouble() + attempts.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveProofOfDeliverySchedule {
        return AdaptiveProofOfDeliverySchedule(
            code = code,
            name = name,
            attempts = attempts,
            summary = summary,
            durationMinutes = durationMinutes,
            checksum = checksum,
            approved = approved,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive proof of delivery schedule in status planned. */
fun newAdaptiveProofOfDeliverySchedule(code: String, name: String): AdaptiveProofOfDeliverySchedule {
    return AdaptiveProofOfDeliverySchedule(
        code = code,
        name = name,
        attempts = 0,
        summary = "",
        durationMinutes = 0,
        checksum = "",
        approved = false,
        reviewer = "",
        status = AdaptiveProofOfDeliveryScheduleStatus.PLANNED,
    )
}
