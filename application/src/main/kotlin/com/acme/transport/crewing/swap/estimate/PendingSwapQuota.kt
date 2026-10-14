package com.acme.transport.crewing.swap.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending swap quota is in its life cycle. */
enum class PendingSwapQuotaStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One pending swap quota, with what the planners need to act on it. */
data class PendingSwapQuota(
    var code: String,
    var label: String,
    var contactEmail: String,
    var threshold: Double,
    var fuelLiters: Double,
    var ownerId: String,
    var discount: Double,
    var batchSize: Int,
    var attempts: Int,
    var distanceKm: Double,
    var status: PendingSwapQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingSwapQuotaStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingSwapQuotaStatus): Boolean {
        when (status) {
            PendingSwapQuotaStatus.PLANNED -> return next == PendingSwapQuotaStatus.CANCELLED
            PendingSwapQuotaStatus.SCHEDULED -> return next == PendingSwapQuotaStatus.IN_PROGRESS || next == PendingSwapQuotaStatus.COMPLETED
            PendingSwapQuotaStatus.IN_PROGRESS -> return next == PendingSwapQuotaStatus.COMPLETED || next == PendingSwapQuotaStatus.CANCELLED
            PendingSwapQuotaStatus.COMPLETED -> return next == PendingSwapQuotaStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingSwapQuotaStatus) {
        if (!canMoveTo(next)) {
            error("pending swap quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedValue(): Double = round((attempts.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): PendingSwapQuota {
        return PendingSwapQuota(
            code = code,
            label = label,
            contactEmail = contactEmail,
            threshold = threshold,
            fuelLiters = fuelLiters,
            ownerId = ownerId,
            discount = discount,
            batchSize = batchSize,
            attempts = attempts,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending swap quota in status planned. */
fun newPendingSwapQuota(code: String, label: String): PendingSwapQuota {
    return PendingSwapQuota(
        code = code,
        label = label,
        contactEmail = "",
        threshold = 0.0,
        fuelLiters = 0.0,
        ownerId = "",
        discount = 0.0,
        batchSize = 0,
        attempts = 0,
        distanceKm = 0.0,
        status = PendingSwapQuotaStatus.PLANNED,
    )
}
