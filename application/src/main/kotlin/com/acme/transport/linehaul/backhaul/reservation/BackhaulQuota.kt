package com.acme.transport.linehaul.backhaul.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul quota is in its life cycle. */
enum class BackhaulQuotaStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a backhaul quota and its current status. */
data class BackhaulQuota(
    var code: String,
    var name: String,
    var windowDays: Int,
    var category: String,
    var minimum: Double,
    var description: String,
    var trailerId: String,
    var status: BackhaulQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulQuotaStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulQuotaStatus): Boolean {
        when (status) {
            BackhaulQuotaStatus.PLANNED -> return next == BackhaulQuotaStatus.IN_PROGRESS || next == BackhaulQuotaStatus.CANCELLED
            BackhaulQuotaStatus.SCHEDULED -> {
                when (next) {
                    BackhaulQuotaStatus.COMPLETED -> return true
                }
                return false
            }
            BackhaulQuotaStatus.IN_PROGRESS -> {
                when (next) {
                    BackhaulQuotaStatus.COMPLETED -> return true
                }
                return false
            }
            BackhaulQuotaStatus.COMPLETED -> return next == BackhaulQuotaStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BackhaulQuotaStatus) {
        if (!canMoveTo(next)) {
            error("backhaul quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"backhaul quota"} $code"

    fun effectiveExposure(): Double = round((windowDays.toDouble() + minimum) * 100) / 100

    fun snapshot(): BackhaulQuota {
        return BackhaulQuota(
            code = code,
            name = name,
            windowDays = windowDays,
            category = category,
            minimum = minimum,
            description = description,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a backhaul quota in status planned. */
fun newBackhaulQuota(code: String, name: String): BackhaulQuota {
    return BackhaulQuota(
        code = code,
        name = name,
        windowDays = 0,
        category = "",
        minimum = 0.0,
        description = "",
        trailerId = "",
        status = BackhaulQuotaStatus.PLANNED,
    )
}
