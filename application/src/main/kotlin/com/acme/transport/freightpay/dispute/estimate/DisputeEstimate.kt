package com.acme.transport.freightpay.dispute.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dispute estimate records of freightpay's dispute work, and the rules, stores, and calculations around
// them.

/** Where a dispute estimate is in its life cycle. */
enum class DisputeEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a dispute estimate and its current status. */
data class DisputeEstimate(
    var code: String,
    var label: String,
    var minimum: Double,
    var expiresAt: Int,
    var batchSize: Int,
    var ownerId: String,
    var region: String,
    var rate: Double,
    var threshold: Double,
    var windowDays: Int,
    var channel: String,
    var status: DisputeEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeEstimateStatus): Boolean {
        when (status) {
            DisputeEstimateStatus.CREATED -> {
                when (next) {
                    DisputeEstimateStatus.RETIRED -> return true
                }
                return false
            }
            DisputeEstimateStatus.VALIDATED -> return next == DisputeEstimateStatus.PUBLISHED || next == DisputeEstimateStatus.RETIRED
            DisputeEstimateStatus.PUBLISHED -> {
                when (next) {
                    DisputeEstimateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeEstimateStatus) {
        if (!canMoveTo(next)) {
            error("dispute estimate $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"dispute estimate"} $code"

    fun projectedTotal(): Double = round((minimum + threshold) * 100) / 100

    fun snapshot(): DisputeEstimate {
        return DisputeEstimate(
            code = code,
            label = label,
            minimum = minimum,
            expiresAt = expiresAt,
            batchSize = batchSize,
            ownerId = ownerId,
            region = region,
            rate = rate,
            threshold = threshold,
            windowDays = windowDays,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a dispute estimate in status created. */
fun newDisputeEstimate(code: String, label: String): DisputeEstimate {
    return DisputeEstimate(
        code = code,
        label = label,
        minimum = 0.0,
        expiresAt = 0,
        batchSize = 0,
        ownerId = "",
        region = "",
        rate = 0.0,
        threshold = 0.0,
        windowDays = 0,
        channel = "",
        status = DisputeEstimateStatus.CREATED,
    )
}
