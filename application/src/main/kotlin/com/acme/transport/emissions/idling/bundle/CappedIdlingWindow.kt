package com.acme.transport.emissions.idling.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped idling window is in its life cycle. */
enum class CappedIdlingWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped idling window. Its code is unique within a tenant. */
data class CappedIdlingWindow(
    var code: String,
    var title: String,
    var lane: String,
    var amount: Double,
    var minimum: Double,
    var capacity: Int,
    var carrier: String,
    var priority: Int,
    var expiresAt: Int,
    var attempts: Int,
    var status: CappedIdlingWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedIdlingWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedIdlingWindowStatus): Boolean {
        when (status) {
            CappedIdlingWindowStatus.DRAFT -> {
                when (next) {
                    CappedIdlingWindowStatus.PENDING, CappedIdlingWindowStatus.ACTIVE -> return true
                }
                return false
            }
            CappedIdlingWindowStatus.PENDING -> return next == CappedIdlingWindowStatus.ACTIVE
            CappedIdlingWindowStatus.ACTIVE -> return next == CappedIdlingWindowStatus.SUSPENDED || next == CappedIdlingWindowStatus.CLOSED
            CappedIdlingWindowStatus.SUSPENDED -> return next == CappedIdlingWindowStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedIdlingWindowStatus) {
        if (!canMoveTo(next)) {
            error("capped idling window $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"capped idling window"} $code"

    fun netExposure(): Double = round((attempts.toDouble() - minimum) * 100) / 100

    fun snapshot(): CappedIdlingWindow {
        return CappedIdlingWindow(
            code = code,
            title = title,
            lane = lane,
            amount = amount,
            minimum = minimum,
            capacity = capacity,
            carrier = carrier,
            priority = priority,
            expiresAt = expiresAt,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a capped idling window in status draft. */
fun newCappedIdlingWindow(code: String, title: String): CappedIdlingWindow {
    return CappedIdlingWindow(
        code = code,
        title = title,
        lane = "",
        amount = 0.0,
        minimum = 0.0,
        capacity = 0,
        carrier = "",
        priority = 0,
        expiresAt = 0,
        attempts = 0,
        status = CappedIdlingWindowStatus.DRAFT,
    )
}
