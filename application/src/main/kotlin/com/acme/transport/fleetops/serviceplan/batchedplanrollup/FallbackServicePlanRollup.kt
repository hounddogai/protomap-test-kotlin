package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback service plan rollup records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a fallback service plan rollup is in its life cycle. */
enum class FallbackServicePlanRollupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One fallback service plan rollup, with what the planners need to act on it. */
data class FallbackServicePlanRollup(
    var code: String,
    var label: String,
    var routeCode: String,
    var pallets: Int,
    var dock: String,
    var updatedAt: Int,
    var maximum: Double,
    var windowDays: Int,
    var status: FallbackServicePlanRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackServicePlanRollupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackServicePlanRollupStatus): Boolean {
        when (status) {
            FallbackServicePlanRollupStatus.DRAFT -> {
                when (next) {
                    FallbackServicePlanRollupStatus.PENDING, FallbackServicePlanRollupStatus.ACTIVE -> return true
                }
                return false
            }
            FallbackServicePlanRollupStatus.PENDING -> {
                when (next) {
                    FallbackServicePlanRollupStatus.SUSPENDED -> return true
                }
                return false
            }
            FallbackServicePlanRollupStatus.ACTIVE -> {
                when (next) {
                    FallbackServicePlanRollupStatus.SUSPENDED, FallbackServicePlanRollupStatus.CLOSED -> return true
                }
                return false
            }
            FallbackServicePlanRollupStatus.SUSPENDED -> return next == FallbackServicePlanRollupStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FallbackServicePlanRollupStatus) {
        if (!canMoveTo(next)) {
            error("fallback service plan rollup $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"fallback service plan rollup"} $code"

    fun adjustedVolume(): Double = round(pallets.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): FallbackServicePlanRollup {
        return FallbackServicePlanRollup(
            code = code,
            label = label,
            routeCode = routeCode,
            pallets = pallets,
            dock = dock,
            updatedAt = updatedAt,
            maximum = maximum,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback service plan rollup in status draft. */
fun newFallbackServicePlanRollup(code: String, label: String): FallbackServicePlanRollup {
    return FallbackServicePlanRollup(
        code = code,
        label = label,
        routeCode = "",
        pallets = 0,
        dock = "",
        updatedAt = 0,
        maximum = 0.0,
        windowDays = 0,
        status = FallbackServicePlanRollupStatus.DRAFT,
    )
}
