package com.acme.transport.crewing.restbreak.milestone

import com.acme.transport.kit.text.normalize

/** Where a fallback rest break override is in its life cycle. */
enum class FallbackRestBreakOverrideStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A fallback rest break override. Its code is unique within a tenant. */
data class FallbackRestBreakOverride(
    var code: String,
    var label: String,
    var archived: Boolean,
    var depot: String,
    var contactEmail: String,
    var distanceKm: Double,
    var status: FallbackRestBreakOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRestBreakOverrideStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRestBreakOverrideStatus): Boolean {
        when (status) {
            FallbackRestBreakOverrideStatus.PLANNED -> {
                when (next) {
                    FallbackRestBreakOverrideStatus.SCHEDULED, FallbackRestBreakOverrideStatus.COMPLETED -> return true
                }
                return false
            }
            FallbackRestBreakOverrideStatus.SCHEDULED -> {
                when (next) {
                    FallbackRestBreakOverrideStatus.IN_PROGRESS -> return true
                }
                return false
            }
            FallbackRestBreakOverrideStatus.IN_PROGRESS -> return next == FallbackRestBreakOverrideStatus.COMPLETED
            FallbackRestBreakOverrideStatus.COMPLETED -> return next == FallbackRestBreakOverrideStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: FallbackRestBreakOverrideStatus) {
        if (!canMoveTo(next)) {
            error("fallback rest break override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = distanceKm

    fun snapshot(): FallbackRestBreakOverride {
        return FallbackRestBreakOverride(
            code = code,
            label = label,
            archived = archived,
            depot = depot,
            contactEmail = contactEmail,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fallback rest break override in status planned. */
fun newFallbackRestBreakOverride(code: String, label: String): FallbackRestBreakOverride {
    return FallbackRestBreakOverride(
        code = code,
        label = label,
        archived = false,
        depot = "",
        contactEmail = "",
        distanceKm = 0.0,
        status = FallbackRestBreakOverrideStatus.PLANNED,
    )
}
