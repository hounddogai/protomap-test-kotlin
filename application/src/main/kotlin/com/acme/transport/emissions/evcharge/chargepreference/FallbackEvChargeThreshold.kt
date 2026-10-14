package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback ev charge threshold is in its life cycle. */
enum class FallbackEvChargeThresholdStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A fallback ev charge threshold. Its code is unique within a tenant. */
data class FallbackEvChargeThreshold(
    var code: String,
    var title: String,
    var createdAt: Int,
    var reviewer: String,
    var active: Boolean,
    var notes: String,
    var category: String,
    var amount: Double,
    var status: FallbackEvChargeThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackEvChargeThresholdStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackEvChargeThresholdStatus): Boolean {
        when (status) {
            FallbackEvChargeThresholdStatus.DRAFT -> return next == FallbackEvChargeThresholdStatus.SUSPENDED
            FallbackEvChargeThresholdStatus.PENDING -> return next == FallbackEvChargeThresholdStatus.SUSPENDED || next == FallbackEvChargeThresholdStatus.CLOSED
            FallbackEvChargeThresholdStatus.ACTIVE -> return next == FallbackEvChargeThresholdStatus.CLOSED
            FallbackEvChargeThresholdStatus.SUSPENDED -> {
                when (next) {
                    FallbackEvChargeThresholdStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackEvChargeThresholdStatus) {
        if (!canMoveTo(next)) {
            error("fallback ev charge threshold $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netVolume(): Double = round((amount + createdAt.toDouble()) * 100) / 100

    fun snapshot(): FallbackEvChargeThreshold {
        return FallbackEvChargeThreshold(
            code = code,
            title = title,
            createdAt = createdAt,
            reviewer = reviewer,
            active = active,
            notes = notes,
            category = category,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a fallback ev charge threshold in status draft. */
fun newFallbackEvChargeThreshold(code: String, title: String): FallbackEvChargeThreshold {
    return FallbackEvChargeThreshold(
        code = code,
        title = title,
        createdAt = 0,
        reviewer = "",
        active = false,
        notes = "",
        category = "",
        amount = 0.0,
        status = FallbackEvChargeThresholdStatus.DRAFT,
    )
}
