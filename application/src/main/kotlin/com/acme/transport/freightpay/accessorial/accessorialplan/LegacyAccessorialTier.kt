package com.acme.transport.freightpay.accessorial.accessorialplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy accessorial tier is in its life cycle. */
enum class LegacyAccessorialTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a legacy accessorial tier and its current status. */
data class LegacyAccessorialTier(
    var code: String,
    var title: String,
    var budget: Double,
    var batchSize: Int,
    var driverId: String,
    var revision: Int,
    var status: LegacyAccessorialTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyAccessorialTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyAccessorialTierStatus): Boolean {
        when (status) {
            LegacyAccessorialTierStatus.PLANNED -> return next == LegacyAccessorialTierStatus.CANCELLED
            LegacyAccessorialTierStatus.SCHEDULED -> return next == LegacyAccessorialTierStatus.IN_PROGRESS || next == LegacyAccessorialTierStatus.CANCELLED
            LegacyAccessorialTierStatus.IN_PROGRESS -> {
                when (next) {
                    LegacyAccessorialTierStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyAccessorialTierStatus.COMPLETED -> {
                when (next) {
                    LegacyAccessorialTierStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyAccessorialTierStatus) {
        if (!canMoveTo(next)) {
            error("legacy accessorial tier $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"legacy accessorial tier"} $code"

    fun weightedTotal(): Double = round((batchSize.toDouble() + budget) * 100) / 100

    fun snapshot(): LegacyAccessorialTier {
        return LegacyAccessorialTier(
            code = code,
            title = title,
            budget = budget,
            batchSize = batchSize,
            driverId = driverId,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy accessorial tier in status planned. */
fun newLegacyAccessorialTier(code: String, title: String): LegacyAccessorialTier {
    return LegacyAccessorialTier(
        code = code,
        title = title,
        budget = 0.0,
        batchSize = 0,
        driverId = "",
        revision = 0,
        status = LegacyAccessorialTierStatus.PLANNED,
    )
}
