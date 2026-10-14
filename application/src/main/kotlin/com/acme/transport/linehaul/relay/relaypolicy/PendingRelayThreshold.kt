package com.acme.transport.linehaul.relay.relaypolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending relay threshold is in its life cycle. */
enum class PendingRelayThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a pending relay threshold and its current status. */
data class PendingRelayThreshold(
    var code: String,
    var name: String,
    var channel: String,
    var score: Double,
    var fuelLiters: Double,
    var category: String,
    var title: String,
    var priority: Int,
    var threshold: Double,
    var startsAt: Int,
    var status: PendingRelayThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRelayThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRelayThresholdStatus): Boolean {
        when (status) {
            PendingRelayThresholdStatus.REQUESTED -> return next == PendingRelayThresholdStatus.REVOKED || next == PendingRelayThresholdStatus.EXPIRED
            PendingRelayThresholdStatus.GRANTED -> return next == PendingRelayThresholdStatus.REVOKED
            PendingRelayThresholdStatus.REVOKED -> {
                when (next) {
                    PendingRelayThresholdStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingRelayThresholdStatus) {
        if (!canMoveTo(next)) {
            error("pending relay threshold $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"pending relay threshold"} $code"

    fun weightedTotal(): Double = round((threshold + fuelLiters) * 100) / 100

    fun snapshot(): PendingRelayThreshold {
        return PendingRelayThreshold(
            code = code,
            name = name,
            channel = channel,
            score = score,
            fuelLiters = fuelLiters,
            category = category,
            title = title,
            priority = priority,
            threshold = threshold,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending relay threshold in status requested. */
fun newPendingRelayThreshold(code: String, name: String): PendingRelayThreshold {
    return PendingRelayThreshold(
        code = code,
        name = name,
        channel = "",
        score = 0.0,
        fuelLiters = 0.0,
        category = "",
        title = "",
        priority = 0,
        threshold = 0.0,
        startsAt = 0,
        status = PendingRelayThresholdStatus.REQUESTED,
    )
}
