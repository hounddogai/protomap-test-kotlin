package com.acme.transport.lastmile.redelivery.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery window is in its life cycle. */
enum class RedeliveryWindowStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A redelivery window. Its code is unique within a tenant. */
data class RedeliveryWindow(
    var code: String,
    var label: String,
    var archived: Boolean,
    var active: Boolean,
    var tags: MutableList<String>,
    var windowDays: Int,
    var currency: String,
    var distanceKm: Double,
    var updatedAt: Int,
    var limit: Int,
    var status: RedeliveryWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryWindowStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryWindowStatus): Boolean {
        when (status) {
            RedeliveryWindowStatus.PLANNED -> return next == RedeliveryWindowStatus.CANCELLED
            RedeliveryWindowStatus.SCHEDULED -> {
                when (next) {
                    RedeliveryWindowStatus.COMPLETED -> return true
                }
                return false
            }
            RedeliveryWindowStatus.IN_PROGRESS -> {
                when (next) {
                    RedeliveryWindowStatus.COMPLETED, RedeliveryWindowStatus.CANCELLED -> return true
                }
                return false
            }
            RedeliveryWindowStatus.COMPLETED -> return next == RedeliveryWindowStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RedeliveryWindowStatus) {
        if (!canMoveTo(next)) {
            error("redelivery window $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"redelivery window"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedTotal(): Double = round((windowDays.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): RedeliveryWindow {
        return RedeliveryWindow(
            code = code,
            label = label,
            archived = archived,
            active = active,
            tags = tags,
            windowDays = windowDays,
            currency = currency,
            distanceKm = distanceKm,
            updatedAt = updatedAt,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a redelivery window in status planned. */
fun newRedeliveryWindow(code: String, label: String): RedeliveryWindow {
    return RedeliveryWindow(
        code = code,
        label = label,
        archived = false,
        active = false,
        tags = mutableListOf<String>(),
        windowDays = 0,
        currency = "",
        distanceKm = 0.0,
        updatedAt = 0,
        limit = 0,
        status = RedeliveryWindowStatus.PLANNED,
    )
}
