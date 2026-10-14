package com.acme.transport.fleetops.utilization.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual utilization variance records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a manual utilization variance is in its life cycle. */
enum class ManualUtilizationVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A manual utilization variance as the utilization team keeps it. */
data class ManualUtilizationVariance(
    var code: String,
    var label: String,
    var attempts: Int,
    var dock: String,
    var quantity: Int,
    var region: String,
    var externalRef: String,
    var active: Boolean,
    var expiresAt: Int,
    var updatedAt: Int,
    var status: ManualUtilizationVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualUtilizationVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualUtilizationVarianceStatus): Boolean {
        when (status) {
            ManualUtilizationVarianceStatus.PROPOSED -> {
                when (next) {
                    ManualUtilizationVarianceStatus.REJECTED -> return true
                }
                return false
            }
            ManualUtilizationVarianceStatus.APPROVED -> return next == ManualUtilizationVarianceStatus.REJECTED
            ManualUtilizationVarianceStatus.REJECTED -> return next == ManualUtilizationVarianceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ManualUtilizationVarianceStatus) {
        if (!canMoveTo(next)) {
            error("manual utilization variance $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedVolume(): Double = round((updatedAt.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): ManualUtilizationVariance {
        return ManualUtilizationVariance(
            code = code,
            label = label,
            attempts = attempts,
            dock = dock,
            quantity = quantity,
            region = region,
            externalRef = externalRef,
            active = active,
            expiresAt = expiresAt,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a manual utilization variance in status proposed. */
fun newManualUtilizationVariance(code: String, label: String): ManualUtilizationVariance {
    return ManualUtilizationVariance(
        code = code,
        label = label,
        attempts = 0,
        dock = "",
        quantity = 0,
        region = "",
        externalRef = "",
        active = false,
        expiresAt = 0,
        updatedAt = 0,
        status = ManualUtilizationVarianceStatus.PROPOSED,
    )
}
