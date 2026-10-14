package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback capacity preference records of linehaul's capacity work, and the rules, stores, and
// calculations around them.

/** Where a fallback capacity preference is in its life cycle. */
enum class FallbackCapacityPreferenceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fallback capacity preference as the capacity team keeps it. */
data class FallbackCapacityPreference(
    var code: String,
    var title: String,
    var startsAt: Int,
    var capacity: Int,
    var endsAt: Int,
    var priority: Int,
    var durationMinutes: Int,
    var routeCode: String,
    var createdAt: Int,
    var depot: String,
    var status: FallbackCapacityPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCapacityPreferenceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCapacityPreferenceStatus): Boolean {
        when (status) {
            FallbackCapacityPreferenceStatus.OPEN -> return next == FallbackCapacityPreferenceStatus.REOPENED
            FallbackCapacityPreferenceStatus.IN_REVIEW -> {
                when (next) {
                    FallbackCapacityPreferenceStatus.RESOLVED -> return true
                }
                return false
            }
            FallbackCapacityPreferenceStatus.RESOLVED -> {
                when (next) {
                    FallbackCapacityPreferenceStatus.REOPENED, FallbackCapacityPreferenceStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackCapacityPreferenceStatus.REOPENED -> {
                when (next) {
                    FallbackCapacityPreferenceStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackCapacityPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("fallback capacity preference $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun weightedLoad(): Double = round((priority.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): FallbackCapacityPreference {
        return FallbackCapacityPreference(
            code = code,
            title = title,
            startsAt = startsAt,
            capacity = capacity,
            endsAt = endsAt,
            priority = priority,
            durationMinutes = durationMinutes,
            routeCode = routeCode,
            createdAt = createdAt,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fallback capacity preference in status open. */
fun newFallbackCapacityPreference(code: String, title: String): FallbackCapacityPreference {
    return FallbackCapacityPreference(
        code = code,
        title = title,
        startsAt = 0,
        capacity = 0,
        endsAt = 0,
        priority = 0,
        durationMinutes = 0,
        routeCode = "",
        createdAt = 0,
        depot = "",
        status = FallbackCapacityPreferenceStatus.OPEN,
    )
}
