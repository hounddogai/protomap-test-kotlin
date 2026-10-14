package com.acme.transport.customs.clearance.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default clearance baseline is in its life cycle. */
enum class DefaultClearanceBaselineStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One default clearance baseline, with what the planners need to act on it. */
data class DefaultClearanceBaseline(
    var code: String,
    var name: String,
    var discount: Double,
    var region: String,
    var quantity: Int,
    var archived: Boolean,
    var tags: MutableList<String>,
    var distanceKm: Double,
    var maximum: Double,
    var status: DefaultClearanceBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultClearanceBaselineStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultClearanceBaselineStatus): Boolean {
        when (status) {
            DefaultClearanceBaselineStatus.OPEN -> return next == DefaultClearanceBaselineStatus.REOPENED
            DefaultClearanceBaselineStatus.IN_REVIEW -> {
                when (next) {
                    DefaultClearanceBaselineStatus.REOPENED -> return true
                }
                return false
            }
            DefaultClearanceBaselineStatus.RESOLVED -> return next == DefaultClearanceBaselineStatus.REOPENED || next == DefaultClearanceBaselineStatus.ARCHIVED
            DefaultClearanceBaselineStatus.REOPENED -> return next == DefaultClearanceBaselineStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DefaultClearanceBaselineStatus) {
        if (!canMoveTo(next)) {
            error("default clearance baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedVolume(): Double = round(maximum * discount * 100) / 100

    fun snapshot(): DefaultClearanceBaseline {
        return DefaultClearanceBaseline(
            code = code,
            name = name,
            discount = discount,
            region = region,
            quantity = quantity,
            archived = archived,
            tags = tags,
            distanceKm = distanceKm,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a default clearance baseline in status open. */
fun newDefaultClearanceBaseline(code: String, name: String): DefaultClearanceBaseline {
    return DefaultClearanceBaseline(
        code = code,
        name = name,
        discount = 0.0,
        region = "",
        quantity = 0,
        archived = false,
        tags = mutableListOf<String>(),
        distanceKm = 0.0,
        maximum = 0.0,
        status = DefaultClearanceBaselineStatus.OPEN,
    )
}
