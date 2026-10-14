package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback service plan profile records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a fallback service plan profile is in its life cycle. */
enum class FallbackServicePlanProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One fallback service plan profile, with what the planners need to act on it. */
data class FallbackServicePlanProfile(
    var code: String,
    var label: String,
    var carrier: String,
    var stopCount: Int,
    var revision: Int,
    var status: FallbackServicePlanProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackServicePlanProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackServicePlanProfileStatus): Boolean {
        when (status) {
            FallbackServicePlanProfileStatus.DRAFT -> return next == FallbackServicePlanProfileStatus.PENDING || next == FallbackServicePlanProfileStatus.ACTIVE
            FallbackServicePlanProfileStatus.PENDING -> return next == FallbackServicePlanProfileStatus.CLOSED
            FallbackServicePlanProfileStatus.ACTIVE -> return next == FallbackServicePlanProfileStatus.SUSPENDED || next == FallbackServicePlanProfileStatus.CLOSED
            FallbackServicePlanProfileStatus.SUSPENDED -> {
                when (next) {
                    FallbackServicePlanProfileStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackServicePlanProfileStatus) {
        if (!canMoveTo(next)) {
            error("fallback service plan profile $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"fallback service plan profile"} $code"

    fun projectedValue(): Double = round((stopCount.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): FallbackServicePlanProfile {
        return FallbackServicePlanProfile(
            code = code,
            label = label,
            carrier = carrier,
            stopCount = stopCount,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback service plan profile in status draft. */
fun newFallbackServicePlanProfile(code: String, label: String): FallbackServicePlanProfile {
    return FallbackServicePlanProfile(
        code = code,
        label = label,
        carrier = "",
        stopCount = 0,
        revision = 0,
        status = FallbackServicePlanProfileStatus.DRAFT,
    )
}
