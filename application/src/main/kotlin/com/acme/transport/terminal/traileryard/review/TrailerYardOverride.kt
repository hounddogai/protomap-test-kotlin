package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard override is in its life cycle. */
enum class TrailerYardOverrideStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A trailer yard override. Its code is unique within a tenant. */
data class TrailerYardOverride(
    var code: String,
    var name: String,
    var limit: Int,
    var amount: Double,
    var minimum: Double,
    var carrier: String,
    var category: String,
    var status: TrailerYardOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardOverrideStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardOverrideStatus): Boolean {
        when (status) {
            TrailerYardOverrideStatus.REQUESTED -> {
                when (next) {
                    TrailerYardOverrideStatus.REVOKED, TrailerYardOverrideStatus.EXPIRED -> return true
                }
                return false
            }
            TrailerYardOverrideStatus.GRANTED -> return next == TrailerYardOverrideStatus.EXPIRED
            TrailerYardOverrideStatus.REVOKED -> {
                when (next) {
                    TrailerYardOverrideStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerYardOverrideStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveExposure(): Double = round((amount - limit.toDouble()) * 100) / 100

    fun snapshot(): TrailerYardOverride {
        return TrailerYardOverride(
            code = code,
            name = name,
            limit = limit,
            amount = amount,
            minimum = minimum,
            carrier = carrier,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a trailer yard override in status requested. */
fun newTrailerYardOverride(code: String, name: String): TrailerYardOverride {
    return TrailerYardOverride(
        code = code,
        name = name,
        limit = 0,
        amount = 0.0,
        minimum = 0.0,
        carrier = "",
        category = "",
        status = TrailerYardOverrideStatus.REQUESTED,
    )
}
