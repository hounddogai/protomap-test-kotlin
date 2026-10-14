package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute threshold is in its life cycle. */
enum class DisputeThresholdStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a dispute threshold and its current status. */
data class DisputeThreshold(
    var code: String,
    var label: String,
    var depot: String,
    var pinned: Boolean,
    var revision: Int,
    var createdAt: Int,
    var pallets: Int,
    var weight: Double,
    var version: Int,
    var status: DisputeThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeThresholdStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeThresholdStatus): Boolean {
        when (status) {
            DisputeThresholdStatus.OPEN -> {
                when (next) {
                    DisputeThresholdStatus.IN_REVIEW, DisputeThresholdStatus.REOPENED -> return true
                }
                return false
            }
            DisputeThresholdStatus.IN_REVIEW -> return next == DisputeThresholdStatus.REOPENED || next == DisputeThresholdStatus.ARCHIVED
            DisputeThresholdStatus.RESOLVED -> {
                when (next) {
                    DisputeThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
            DisputeThresholdStatus.REOPENED -> {
                when (next) {
                    DisputeThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeThresholdStatus) {
        if (!canMoveTo(next)) {
            error("dispute threshold $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedExposure(): Double = round(pallets.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): DisputeThreshold {
        return DisputeThreshold(
            code = code,
            label = label,
            depot = depot,
            pinned = pinned,
            revision = revision,
            createdAt = createdAt,
            pallets = pallets,
            weight = weight,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a dispute threshold in status open. */
fun newDisputeThreshold(code: String, label: String): DisputeThreshold {
    return DisputeThreshold(
        code = code,
        label = label,
        depot = "",
        pinned = false,
        revision = 0,
        createdAt = 0,
        pallets = 0,
        weight = 0.0,
        version = 0,
        status = DisputeThresholdStatus.OPEN,
    )
}
