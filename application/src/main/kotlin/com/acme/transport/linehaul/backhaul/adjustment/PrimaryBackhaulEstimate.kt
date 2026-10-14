package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary backhaul estimate is in its life cycle. */
enum class PrimaryBackhaulEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A primary backhaul estimate. Its code is unique within a tenant. */
data class PrimaryBackhaulEstimate(
    var code: String,
    var label: String,
    var currency: String,
    var fuelLiters: Double,
    var priority: Int,
    var active: Boolean,
    var trailerId: String,
    var notes: String,
    var score: Double,
    var threshold: Double,
    var status: PrimaryBackhaulEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryBackhaulEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryBackhaulEstimateStatus): Boolean {
        when (status) {
            PrimaryBackhaulEstimateStatus.OPEN -> return next == PrimaryBackhaulEstimateStatus.IN_REVIEW || next == PrimaryBackhaulEstimateStatus.RESOLVED
            PrimaryBackhaulEstimateStatus.IN_REVIEW -> return next == PrimaryBackhaulEstimateStatus.REOPENED
            PrimaryBackhaulEstimateStatus.RESOLVED -> return next == PrimaryBackhaulEstimateStatus.REOPENED || next == PrimaryBackhaulEstimateStatus.ARCHIVED
            PrimaryBackhaulEstimateStatus.REOPENED -> return next == PrimaryBackhaulEstimateStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PrimaryBackhaulEstimateStatus) {
        if (!canMoveTo(next)) {
            error("primary backhaul estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary backhaul estimate"} $code"

    fun weightedVolume(): Double = round((score - threshold) * 100) / 100

    fun snapshot(): PrimaryBackhaulEstimate {
        return PrimaryBackhaulEstimate(
            code = code,
            label = label,
            currency = currency,
            fuelLiters = fuelLiters,
            priority = priority,
            active = active,
            trailerId = trailerId,
            notes = notes,
            score = score,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary backhaul estimate in status open. */
fun newPrimaryBackhaulEstimate(code: String, label: String): PrimaryBackhaulEstimate {
    return PrimaryBackhaulEstimate(
        code = code,
        label = label,
        currency = "",
        fuelLiters = 0.0,
        priority = 0,
        active = false,
        trailerId = "",
        notes = "",
        score = 0.0,
        threshold = 0.0,
        status = PrimaryBackhaulEstimateStatus.OPEN,
    )
}
