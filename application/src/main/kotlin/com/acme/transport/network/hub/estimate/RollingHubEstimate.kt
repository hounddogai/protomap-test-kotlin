package com.acme.transport.network.hub.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling hub estimate records of network's hub work, and the rules, stores, and calculations around
// them.

/** Where a rolling hub estimate is in its life cycle. */
enum class RollingHubEstimateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a rolling hub estimate and its current status. */
data class RollingHubEstimate(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var quantity: Int,
    var updatedAt: Int,
    var priority: Int,
    var version: Int,
    var approved: Boolean,
    var depot: String,
    var status: RollingHubEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingHubEstimateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingHubEstimateStatus): Boolean {
        when (status) {
            RollingHubEstimateStatus.REQUESTED -> return next == RollingHubEstimateStatus.GRANTED
            RollingHubEstimateStatus.GRANTED -> {
                when (next) {
                    RollingHubEstimateStatus.REVOKED, RollingHubEstimateStatus.EXPIRED -> return true
                }
                return false
            }
            RollingHubEstimateStatus.REVOKED -> {
                when (next) {
                    RollingHubEstimateStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingHubEstimateStatus) {
        if (!canMoveTo(next)) {
            error("rolling hub estimate $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"rolling hub estimate"} $code"

    fun projectedTotal(): Double = round(quantity.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): RollingHubEstimate {
        return RollingHubEstimate(
            code = code,
            label = label,
            expiresAt = expiresAt,
            quantity = quantity,
            updatedAt = updatedAt,
            priority = priority,
            version = version,
            approved = approved,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rolling hub estimate in status requested. */
fun newRollingHubEstimate(code: String, label: String): RollingHubEstimate {
    return RollingHubEstimate(
        code = code,
        label = label,
        expiresAt = 0,
        quantity = 0,
        updatedAt = 0,
        priority = 0,
        version = 0,
        approved = false,
        depot = "",
        status = RollingHubEstimateStatus.REQUESTED,
    )
}
