package com.acme.transport.customs.broker.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker adjustment is in its life cycle. */
enum class BrokerAdjustmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One broker adjustment, with what the planners need to act on it. */
data class BrokerAdjustment(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var ratio: Double,
    var driverId: String,
    var label: String,
    var distanceKm: Double,
    var pallets: Int,
    var trailerId: String,
    var endsAt: Int,
    var routeCode: String,
    var status: BrokerAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerAdjustmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerAdjustmentStatus): Boolean {
        when (status) {
            BrokerAdjustmentStatus.PLANNED -> return next == BrokerAdjustmentStatus.IN_PROGRESS || next == BrokerAdjustmentStatus.CANCELLED
            BrokerAdjustmentStatus.SCHEDULED -> {
                when (next) {
                    BrokerAdjustmentStatus.IN_PROGRESS -> return true
                }
                return false
            }
            BrokerAdjustmentStatus.IN_PROGRESS -> {
                when (next) {
                    BrokerAdjustmentStatus.COMPLETED -> return true
                }
                return false
            }
            BrokerAdjustmentStatus.COMPLETED -> return next == BrokerAdjustmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BrokerAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("broker adjustment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($title)"

    fun netExposure(): Double = round(ratio * distanceKm * 100) / 100

    fun snapshot(): BrokerAdjustment {
        return BrokerAdjustment(
            code = code,
            title = title,
            pinned = pinned,
            ratio = ratio,
            driverId = driverId,
            label = label,
            distanceKm = distanceKm,
            pallets = pallets,
            trailerId = trailerId,
            endsAt = endsAt,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a broker adjustment in status planned. */
fun newBrokerAdjustment(code: String, title: String): BrokerAdjustment {
    return BrokerAdjustment(
        code = code,
        title = title,
        pinned = false,
        ratio = 0.0,
        driverId = "",
        label = "",
        distanceKm = 0.0,
        pallets = 0,
        trailerId = "",
        endsAt = 0,
        routeCode = "",
        status = BrokerAdjustmentStatus.PLANNED,
    )
}
