package com.acme.transport.network.servicearea.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback service area quota is in its life cycle. */
enum class FallbackServiceAreaQuotaStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a fallback service area quota and its current status. */
data class FallbackServiceAreaQuota(
    var code: String,
    var title: String,
    var stopCount: Int,
    var currency: String,
    var limit: Int,
    var visible: Boolean,
    var unit: String,
    var pallets: Int,
    var status: FallbackServiceAreaQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackServiceAreaQuotaStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackServiceAreaQuotaStatus): Boolean {
        when (status) {
            FallbackServiceAreaQuotaStatus.OPEN -> {
                when (next) {
                    FallbackServiceAreaQuotaStatus.RESOLVED, FallbackServiceAreaQuotaStatus.REOPENED -> return true
                }
                return false
            }
            FallbackServiceAreaQuotaStatus.IN_REVIEW -> return next == FallbackServiceAreaQuotaStatus.REOPENED || next == FallbackServiceAreaQuotaStatus.ARCHIVED
            FallbackServiceAreaQuotaStatus.RESOLVED -> return next == FallbackServiceAreaQuotaStatus.ARCHIVED
            FallbackServiceAreaQuotaStatus.REOPENED -> return next == FallbackServiceAreaQuotaStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FallbackServiceAreaQuotaStatus) {
        if (!canMoveTo(next)) {
            error("fallback service area quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedTotal(): Double = round((stopCount.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): FallbackServiceAreaQuota {
        return FallbackServiceAreaQuota(
            code = code,
            title = title,
            stopCount = stopCount,
            currency = currency,
            limit = limit,
            visible = visible,
            unit = unit,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fallback service area quota in status open. */
fun newFallbackServiceAreaQuota(code: String, title: String): FallbackServiceAreaQuota {
    return FallbackServiceAreaQuota(
        code = code,
        title = title,
        stopCount = 0,
        currency = "",
        limit = 0,
        visible = false,
        unit = "",
        pallets = 0,
        status = FallbackServiceAreaQuotaStatus.OPEN,
    )
}
