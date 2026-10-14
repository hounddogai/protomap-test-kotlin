package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer override is in its life cycle. */
enum class TrailerOverrideStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A trailer override. Its code is unique within a tenant. */
data class TrailerOverride(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var endsAt: Int,
    var expiresAt: Int,
    var unit: String,
    var ceiling: Double,
    var windowDays: Int,
    var status: TrailerOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerOverrideStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerOverrideStatus): Boolean {
        when (status) {
            TrailerOverrideStatus.PROPOSED -> return next == TrailerOverrideStatus.WITHDRAWN
            TrailerOverrideStatus.APPROVED -> {
                when (next) {
                    TrailerOverrideStatus.WITHDRAWN -> return true
                }
                return false
            }
            TrailerOverrideStatus.REJECTED -> return next == TrailerOverrideStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TrailerOverrideStatus) {
        if (!canMoveTo(next)) {
            error("trailer override $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun effectiveLoad(): Double = round((expiresAt.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): TrailerOverride {
        return TrailerOverride(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            endsAt = endsAt,
            expiresAt = expiresAt,
            unit = unit,
            ceiling = ceiling,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a trailer override in status proposed. */
fun newTrailerOverride(code: String, title: String): TrailerOverride {
    return TrailerOverride(
        code = code,
        title = title,
        fuelLiters = 0.0,
        endsAt = 0,
        expiresAt = 0,
        unit = "",
        ceiling = 0.0,
        windowDays = 0,
        status = TrailerOverrideStatus.PROPOSED,
    )
}
