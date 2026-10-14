package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a availability rollup is in its life cycle. */
enum class AvailabilityRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a availability rollup and its current status. */
data class AvailabilityRollup(
    var code: String,
    var label: String,
    var endsAt: Int,
    var quantity: Int,
    var threshold: Double,
    var contactEmail: String,
    var lane: String,
    var status: AvailabilityRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityRollupStatus): Boolean {
        when (status) {
            AvailabilityRollupStatus.OPEN -> return next == AvailabilityRollupStatus.REOPENED
            AvailabilityRollupStatus.IN_REVIEW -> {
                when (next) {
                    AvailabilityRollupStatus.RESOLVED, AvailabilityRollupStatus.ARCHIVED -> return true
                }
                return false
            }
            AvailabilityRollupStatus.RESOLVED -> return next == AvailabilityRollupStatus.REOPENED
            AvailabilityRollupStatus.REOPENED -> return next == AvailabilityRollupStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AvailabilityRollupStatus) {
        if (!canMoveTo(next)) {
            error("availability rollup $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"availability rollup"} $code"

    fun effectiveLoad(): Double = round(quantity.toDouble() * threshold * 100) / 100

    fun snapshot(): AvailabilityRollup {
        return AvailabilityRollup(
            code = code,
            label = label,
            endsAt = endsAt,
            quantity = quantity,
            threshold = threshold,
            contactEmail = contactEmail,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a availability rollup in status open. */
fun newAvailabilityRollup(code: String, label: String): AvailabilityRollup {
    return AvailabilityRollup(
        code = code,
        label = label,
        endsAt = 0,
        quantity = 0,
        threshold = 0.0,
        contactEmail = "",
        lane = "",
        status = AvailabilityRollupStatus.OPEN,
    )
}
