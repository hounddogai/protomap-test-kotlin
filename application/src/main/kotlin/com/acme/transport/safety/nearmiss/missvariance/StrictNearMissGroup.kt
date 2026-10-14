package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict near miss group is in its life cycle. */
enum class StrictNearMissGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One strict near miss group, with what the planners need to act on it. */
data class StrictNearMissGroup(
    var code: String,
    var name: String,
    var summary: String,
    var region: String,
    var odometer: Int,
    var dock: String,
    var cost: Double,
    var channel: String,
    var status: StrictNearMissGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictNearMissGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictNearMissGroupStatus): Boolean {
        when (status) {
            StrictNearMissGroupStatus.DRAFT -> return next == StrictNearMissGroupStatus.SUSPENDED || next == StrictNearMissGroupStatus.CLOSED
            StrictNearMissGroupStatus.PENDING -> return next == StrictNearMissGroupStatus.SUSPENDED || next == StrictNearMissGroupStatus.CLOSED
            StrictNearMissGroupStatus.ACTIVE -> {
                when (next) {
                    StrictNearMissGroupStatus.SUSPENDED -> return true
                }
                return false
            }
            StrictNearMissGroupStatus.SUSPENDED -> {
                when (next) {
                    StrictNearMissGroupStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictNearMissGroupStatus) {
        if (!canMoveTo(next)) {
            error("strict near miss group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict near miss group"} $code"

    fun netVolume(): Double = round(cost * odometer.toDouble() * 100) / 100

    fun snapshot(): StrictNearMissGroup {
        return StrictNearMissGroup(
            code = code,
            name = name,
            summary = summary,
            region = region,
            odometer = odometer,
            dock = dock,
            cost = cost,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a strict near miss group in status draft. */
fun newStrictNearMissGroup(code: String, name: String): StrictNearMissGroup {
    return StrictNearMissGroup(
        code = code,
        name = name,
        summary = "",
        region = "",
        odometer = 0,
        dock = "",
        cost = 0.0,
        channel = "",
        status = StrictNearMissGroupStatus.DRAFT,
    )
}
