package com.acme.transport.network.servicearea.areatarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict service area adjustment is in its life cycle. */
enum class StrictServiceAreaAdjustmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict service area adjustment. Its code is unique within a tenant. */
data class StrictServiceAreaAdjustment(
    var code: String,
    var title: String,
    var retries: Int,
    var quantity: Int,
    var ownerId: String,
    var windowDays: Int,
    var status: StrictServiceAreaAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictServiceAreaAdjustmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictServiceAreaAdjustmentStatus): Boolean {
        when (status) {
            StrictServiceAreaAdjustmentStatus.OPEN -> return next == StrictServiceAreaAdjustmentStatus.RESOLVED
            StrictServiceAreaAdjustmentStatus.IN_REVIEW -> return next == StrictServiceAreaAdjustmentStatus.RESOLVED || next == StrictServiceAreaAdjustmentStatus.REOPENED
            StrictServiceAreaAdjustmentStatus.RESOLVED -> {
                when (next) {
                    StrictServiceAreaAdjustmentStatus.REOPENED, StrictServiceAreaAdjustmentStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictServiceAreaAdjustmentStatus.REOPENED -> {
                when (next) {
                    StrictServiceAreaAdjustmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictServiceAreaAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("strict service area adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun netExposure(): Double = round(quantity.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): StrictServiceAreaAdjustment {
        return StrictServiceAreaAdjustment(
            code = code,
            title = title,
            retries = retries,
            quantity = quantity,
            ownerId = ownerId,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict service area adjustment in status open. */
fun newStrictServiceAreaAdjustment(code: String, title: String): StrictServiceAreaAdjustment {
    return StrictServiceAreaAdjustment(
        code = code,
        title = title,
        retries = 0,
        quantity = 0,
        ownerId = "",
        windowDays = 0,
        status = StrictServiceAreaAdjustmentStatus.OPEN,
    )
}
