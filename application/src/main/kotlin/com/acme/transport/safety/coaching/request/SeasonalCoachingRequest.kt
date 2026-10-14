package com.acme.transport.safety.coaching.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal coaching request records of safety's coaching work, and the rules, stores, and calculations
// around them.

/** Where a seasonal coaching request is in its life cycle. */
enum class SeasonalCoachingRequestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One seasonal coaching request, with what the planners need to act on it. */
data class SeasonalCoachingRequest(
    var code: String,
    var title: String,
    var label: String,
    var cost: Double,
    var loadKg: Double,
    var reviewer: String,
    var stopCount: Int,
    var description: String,
    var status: SeasonalCoachingRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCoachingRequestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCoachingRequestStatus): Boolean {
        when (status) {
            SeasonalCoachingRequestStatus.DRAFT -> return next == SeasonalCoachingRequestStatus.SUSPENDED || next == SeasonalCoachingRequestStatus.CLOSED
            SeasonalCoachingRequestStatus.PENDING -> return next == SeasonalCoachingRequestStatus.ACTIVE
            SeasonalCoachingRequestStatus.ACTIVE -> {
                when (next) {
                    SeasonalCoachingRequestStatus.SUSPENDED, SeasonalCoachingRequestStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalCoachingRequestStatus.SUSPENDED -> {
                when (next) {
                    SeasonalCoachingRequestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalCoachingRequestStatus) {
        if (!canMoveTo(next)) {
            error("seasonal coaching request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun effectiveTotal(): Double = round((cost - loadKg) * 100) / 100

    fun snapshot(): SeasonalCoachingRequest {
        return SeasonalCoachingRequest(
            code = code,
            title = title,
            label = label,
            cost = cost,
            loadKg = loadKg,
            reviewer = reviewer,
            stopCount = stopCount,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal coaching request in status draft. */
fun newSeasonalCoachingRequest(code: String, title: String): SeasonalCoachingRequest {
    return SeasonalCoachingRequest(
        code = code,
        title = title,
        label = "",
        cost = 0.0,
        loadKg = 0.0,
        reviewer = "",
        stopCount = 0,
        description = "",
        status = SeasonalCoachingRequestStatus.DRAFT,
    )
}
