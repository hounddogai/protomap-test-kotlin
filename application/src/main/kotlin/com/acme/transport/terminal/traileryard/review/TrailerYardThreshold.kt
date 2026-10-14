package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard threshold is in its life cycle. */
enum class TrailerYardThresholdStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A trailer yard threshold as the trailer yard team keeps it. */
data class TrailerYardThreshold(
    var code: String,
    var title: String,
    var pallets: Int,
    var approved: Boolean,
    var etaMinutes: Int,
    var carrier: String,
    var status: TrailerYardThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardThresholdStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardThresholdStatus): Boolean {
        when (status) {
            TrailerYardThresholdStatus.DRAFT -> return next == TrailerYardThresholdStatus.PENDING
            TrailerYardThresholdStatus.PENDING -> {
                when (next) {
                    TrailerYardThresholdStatus.CLOSED -> return true
                }
                return false
            }
            TrailerYardThresholdStatus.ACTIVE -> {
                when (next) {
                    TrailerYardThresholdStatus.SUSPENDED, TrailerYardThresholdStatus.CLOSED -> return true
                }
                return false
            }
            TrailerYardThresholdStatus.SUSPENDED -> return next == TrailerYardThresholdStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TrailerYardThresholdStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedTotal(): Double = round((etaMinutes.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): TrailerYardThreshold {
        return TrailerYardThreshold(
            code = code,
            title = title,
            pallets = pallets,
            approved = approved,
            etaMinutes = etaMinutes,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a trailer yard threshold in status draft. */
fun newTrailerYardThreshold(code: String, title: String): TrailerYardThreshold {
    return TrailerYardThreshold(
        code = code,
        title = title,
        pallets = 0,
        approved = false,
        etaMinutes = 0,
        carrier = "",
        status = TrailerYardThresholdStatus.DRAFT,
    )
}
