package com.acme.transport.network.servicearea.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive service area checkpoint is in its life cycle. */
enum class AdaptiveServiceAreaCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive service area checkpoint. Its code is unique within a tenant. */
data class AdaptiveServiceAreaCheckpoint(
    var code: String,
    var title: String,
    var category: String,
    var amount: Double,
    var approved: Boolean,
    var segment: String,
    var endsAt: Int,
    var timezone: String,
    var trailerId: String,
    var carrier: String,
    var status: AdaptiveServiceAreaCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveServiceAreaCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveServiceAreaCheckpointStatus): Boolean {
        when (status) {
            AdaptiveServiceAreaCheckpointStatus.IDLE -> {
                when (next) {
                    AdaptiveServiceAreaCheckpointStatus.READY, AdaptiveServiceAreaCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveServiceAreaCheckpointStatus.WARMING -> {
                when (next) {
                    AdaptiveServiceAreaCheckpointStatus.READY, AdaptiveServiceAreaCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveServiceAreaCheckpointStatus.READY -> {
                when (next) {
                    AdaptiveServiceAreaCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveServiceAreaCheckpointStatus.DRAINING -> return next == AdaptiveServiceAreaCheckpointStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AdaptiveServiceAreaCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("adaptive service area checkpoint $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun weightedExposure(): Double = round((amount + endsAt.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveServiceAreaCheckpoint {
        return AdaptiveServiceAreaCheckpoint(
            code = code,
            title = title,
            category = category,
            amount = amount,
            approved = approved,
            segment = segment,
            endsAt = endsAt,
            timezone = timezone,
            trailerId = trailerId,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a adaptive service area checkpoint in status idle. */
fun newAdaptiveServiceAreaCheckpoint(code: String, title: String): AdaptiveServiceAreaCheckpoint {
    return AdaptiveServiceAreaCheckpoint(
        code = code,
        title = title,
        category = "",
        amount = 0.0,
        approved = false,
        segment = "",
        endsAt = 0,
        timezone = "",
        trailerId = "",
        carrier = "",
        status = AdaptiveServiceAreaCheckpointStatus.IDLE,
    )
}
