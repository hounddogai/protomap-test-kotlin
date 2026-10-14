package com.acme.transport.network.node.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback node variance is in its life cycle. */
enum class FallbackNodeVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One fallback node variance, with what the planners need to act on it. */
data class FallbackNodeVariance(
    var code: String,
    var label: String,
    var startsAt: Int,
    var approved: Boolean,
    var phone: String,
    var margin: Double,
    var batchSize: Int,
    var ownerId: String,
    var quantity: Int,
    var status: FallbackNodeVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackNodeVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackNodeVarianceStatus): Boolean {
        when (status) {
            FallbackNodeVarianceStatus.PLANNED -> {
                when (next) {
                    FallbackNodeVarianceStatus.SCHEDULED, FallbackNodeVarianceStatus.CANCELLED -> return true
                }
                return false
            }
            FallbackNodeVarianceStatus.SCHEDULED -> {
                when (next) {
                    FallbackNodeVarianceStatus.CANCELLED -> return true
                }
                return false
            }
            FallbackNodeVarianceStatus.IN_PROGRESS -> {
                when (next) {
                    FallbackNodeVarianceStatus.COMPLETED, FallbackNodeVarianceStatus.CANCELLED -> return true
                }
                return false
            }
            FallbackNodeVarianceStatus.COMPLETED -> return next == FallbackNodeVarianceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: FallbackNodeVarianceStatus) {
        if (!canMoveTo(next)) {
            error("fallback node variance $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun netLoad(): Double = round(batchSize.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): FallbackNodeVariance {
        return FallbackNodeVariance(
            code = code,
            label = label,
            startsAt = startsAt,
            approved = approved,
            phone = phone,
            margin = margin,
            batchSize = batchSize,
            ownerId = ownerId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback node variance in status planned. */
fun newFallbackNodeVariance(code: String, label: String): FallbackNodeVariance {
    return FallbackNodeVariance(
        code = code,
        label = label,
        startsAt = 0,
        approved = false,
        phone = "",
        margin = 0.0,
        batchSize = 0,
        ownerId = "",
        quantity = 0,
        status = FallbackNodeVarianceStatus.PLANNED,
    )
}
