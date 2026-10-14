package com.acme.transport.linehaul.relay.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending relay tier is in its life cycle. */
enum class PendingRelayTierStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending relay tier. Its code is unique within a tenant. */
data class PendingRelayTier(
    var code: String,
    var title: String,
    var cost: Double,
    var category: String,
    var dock: String,
    var updatedAt: Int,
    var floorValue: Double,
    var distanceKm: Double,
    var status: PendingRelayTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRelayTierStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRelayTierStatus): Boolean {
        when (status) {
            PendingRelayTierStatus.OPEN -> {
                when (next) {
                    PendingRelayTierStatus.RESOLVED, PendingRelayTierStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingRelayTierStatus.IN_REVIEW -> {
                when (next) {
                    PendingRelayTierStatus.RESOLVED -> return true
                }
                return false
            }
            PendingRelayTierStatus.RESOLVED -> return next == PendingRelayTierStatus.REOPENED || next == PendingRelayTierStatus.ARCHIVED
            PendingRelayTierStatus.REOPENED -> {
                when (next) {
                    PendingRelayTierStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingRelayTierStatus) {
        if (!canMoveTo(next)) {
            error("pending relay tier $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedValue(): Double = round(cost * updatedAt.toDouble() * 100) / 100

    fun snapshot(): PendingRelayTier {
        return PendingRelayTier(
            code = code,
            title = title,
            cost = cost,
            category = category,
            dock = dock,
            updatedAt = updatedAt,
            floorValue = floorValue,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a pending relay tier in status open. */
fun newPendingRelayTier(code: String, title: String): PendingRelayTier {
    return PendingRelayTier(
        code = code,
        title = title,
        cost = 0.0,
        category = "",
        dock = "",
        updatedAt = 0,
        floorValue = 0.0,
        distanceKm = 0.0,
        status = PendingRelayTierStatus.OPEN,
    )
}
