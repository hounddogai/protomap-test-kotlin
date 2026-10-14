package com.acme.transport.fleetops.utilization.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The utilization window records of fleetops's utilization work, and the rules, stores, and calculations
// around them.

/** Where a utilization window is in its life cycle. */
enum class UtilizationWindowStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A utilization window. Its code is unique within a tenant. */
data class UtilizationWindow(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var updatedAt: Int,
    var priority: Int,
    var attempts: Int,
    var archived: Boolean,
    var depot: String,
    var version: Int,
    var reviewer: String,
    var status: UtilizationWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationWindowStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationWindowStatus): Boolean {
        when (status) {
            UtilizationWindowStatus.PLANNED -> return next == UtilizationWindowStatus.COMPLETED
            UtilizationWindowStatus.SCHEDULED -> {
                when (next) {
                    UtilizationWindowStatus.IN_PROGRESS, UtilizationWindowStatus.COMPLETED -> return true
                }
                return false
            }
            UtilizationWindowStatus.IN_PROGRESS -> return next == UtilizationWindowStatus.COMPLETED
            UtilizationWindowStatus.COMPLETED -> return next == UtilizationWindowStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: UtilizationWindowStatus) {
        if (!canMoveTo(next)) {
            error("utilization window $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveTotal(): Double = round(attempts.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): UtilizationWindow {
        return UtilizationWindow(
            code = code,
            name = name,
            tags = tags,
            updatedAt = updatedAt,
            priority = priority,
            attempts = attempts,
            archived = archived,
            depot = depot,
            version = version,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a utilization window in status planned. */
fun newUtilizationWindow(code: String, name: String): UtilizationWindow {
    return UtilizationWindow(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        updatedAt = 0,
        priority = 0,
        attempts = 0,
        archived = false,
        depot = "",
        version = 0,
        reviewer = "",
        status = UtilizationWindowStatus.PLANNED,
    )
}
