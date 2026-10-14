package com.acme.transport.crewing.swap.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict swap request records of crewing's swap work, and the rules, stores, and calculations around
// them.

/** Where a strict swap request is in its life cycle. */
enum class StrictSwapRequestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict swap request. Its code is unique within a tenant. */
data class StrictSwapRequest(
    var code: String,
    var title: String,
    var priority: Int,
    var country: String,
    var externalRef: String,
    var durationMinutes: Int,
    var active: Boolean,
    var status: StrictSwapRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictSwapRequestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictSwapRequestStatus): Boolean {
        when (status) {
            StrictSwapRequestStatus.OPEN -> {
                when (next) {
                    StrictSwapRequestStatus.IN_REVIEW, StrictSwapRequestStatus.RESOLVED -> return true
                }
                return false
            }
            StrictSwapRequestStatus.IN_REVIEW -> {
                when (next) {
                    StrictSwapRequestStatus.REOPENED -> return true
                }
                return false
            }
            StrictSwapRequestStatus.RESOLVED -> return next == StrictSwapRequestStatus.ARCHIVED
            StrictSwapRequestStatus.REOPENED -> {
                when (next) {
                    StrictSwapRequestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictSwapRequestStatus) {
        if (!canMoveTo(next)) {
            error("strict swap request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict swap request"} $code"

    fun projectedLoad(): Double = round((durationMinutes.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): StrictSwapRequest {
        return StrictSwapRequest(
            code = code,
            title = title,
            priority = priority,
            country = country,
            externalRef = externalRef,
            durationMinutes = durationMinutes,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a strict swap request in status open. */
fun newStrictSwapRequest(code: String, title: String): StrictSwapRequest {
    return StrictSwapRequest(
        code = code,
        title = title,
        priority = 0,
        country = "",
        externalRef = "",
        durationMinutes = 0,
        active = false,
        status = StrictSwapRequestStatus.OPEN,
    )
}
