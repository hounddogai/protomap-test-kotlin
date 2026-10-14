package com.acme.transport.fleetops.tire.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default tire estimate is in its life cycle. */
enum class DefaultTireEstimateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One default tire estimate, with what the planners need to act on it. */
data class DefaultTireEstimate(
    var code: String,
    var name: String,
    var capacity: Int,
    var threshold: Double,
    var reviewer: String,
    var windowDays: Int,
    var status: DefaultTireEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTireEstimateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTireEstimateStatus): Boolean {
        when (status) {
            DefaultTireEstimateStatus.DRAFT -> return next == DefaultTireEstimateStatus.SUSPENDED
            DefaultTireEstimateStatus.PENDING -> {
                when (next) {
                    DefaultTireEstimateStatus.CLOSED -> return true
                }
                return false
            }
            DefaultTireEstimateStatus.ACTIVE -> return next == DefaultTireEstimateStatus.SUSPENDED
            DefaultTireEstimateStatus.SUSPENDED -> {
                when (next) {
                    DefaultTireEstimateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultTireEstimateStatus) {
        if (!canMoveTo(next)) {
            error("default tire estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default tire estimate"} $code"

    fun weightedVolume(): Double = round(capacity.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): DefaultTireEstimate {
        return DefaultTireEstimate(
            code = code,
            name = name,
            capacity = capacity,
            threshold = threshold,
            reviewer = reviewer,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default tire estimate in status draft. */
fun newDefaultTireEstimate(code: String, name: String): DefaultTireEstimate {
    return DefaultTireEstimate(
        code = code,
        name = name,
        capacity = 0,
        threshold = 0.0,
        reviewer = "",
        windowDays = 0,
        status = DefaultTireEstimateStatus.DRAFT,
    )
}
