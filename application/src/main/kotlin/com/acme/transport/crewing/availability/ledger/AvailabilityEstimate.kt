package com.acme.transport.crewing.availability.ledger

import com.acme.transport.kit.text.normalize

/** Where a availability estimate is in its life cycle. */
enum class AvailabilityEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One availability estimate, with what the planners need to act on it. */
data class AvailabilityEstimate(
    var code: String,
    var title: String,
    var label: String,
    var timezone: String,
    var loadKg: Double,
    var status: AvailabilityEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityEstimateStatus): Boolean {
        when (status) {
            AvailabilityEstimateStatus.CREATED -> {
                when (next) {
                    AvailabilityEstimateStatus.RETIRED -> return true
                }
                return false
            }
            AvailabilityEstimateStatus.VALIDATED -> {
                when (next) {
                    AvailabilityEstimateStatus.PUBLISHED, AvailabilityEstimateStatus.RETIRED -> return true
                }
                return false
            }
            AvailabilityEstimateStatus.PUBLISHED -> return next == AvailabilityEstimateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AvailabilityEstimateStatus) {
        if (!canMoveTo(next)) {
            error("availability estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun measuredValue(): Double = loadKg

    fun snapshot(): AvailabilityEstimate {
        return AvailabilityEstimate(
            code = code,
            title = title,
            label = label,
            timezone = timezone,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a availability estimate in status created. */
fun newAvailabilityEstimate(code: String, title: String): AvailabilityEstimate {
    return AvailabilityEstimate(
        code = code,
        title = title,
        label = "",
        timezone = "",
        loadKg = 0.0,
        status = AvailabilityEstimateStatus.CREATED,
    )
}
