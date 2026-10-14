package com.acme.transport.network.transittime.timerequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default transit time checkpoint is in its life cycle. */
enum class DefaultTransitTimeCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A default transit time checkpoint as the transit time team keeps it. */
data class DefaultTransitTimeCheckpoint(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var ratio: Double,
    var trailerId: String,
    var amount: Double,
    var threshold: Double,
    var locale: String,
    var externalRef: String,
    var status: DefaultTransitTimeCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTransitTimeCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTransitTimeCheckpointStatus): Boolean {
        when (status) {
            DefaultTransitTimeCheckpointStatus.IDLE -> {
                when (next) {
                    DefaultTransitTimeCheckpointStatus.WARMING, DefaultTransitTimeCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            DefaultTransitTimeCheckpointStatus.WARMING -> {
                when (next) {
                    DefaultTransitTimeCheckpointStatus.READY, DefaultTransitTimeCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            DefaultTransitTimeCheckpointStatus.READY -> return next == DefaultTransitTimeCheckpointStatus.STOPPED
            DefaultTransitTimeCheckpointStatus.DRAINING -> {
                when (next) {
                    DefaultTransitTimeCheckpointStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultTransitTimeCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("default transit time checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedValue(): Double = round((amount + threshold) * 100) / 100

    fun snapshot(): DefaultTransitTimeCheckpoint {
        return DefaultTransitTimeCheckpoint(
            code = code,
            name = name,
            pinned = pinned,
            ratio = ratio,
            trailerId = trailerId,
            amount = amount,
            threshold = threshold,
            locale = locale,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a default transit time checkpoint in status idle. */
fun newDefaultTransitTimeCheckpoint(code: String, name: String): DefaultTransitTimeCheckpoint {
    return DefaultTransitTimeCheckpoint(
        code = code,
        name = name,
        pinned = false,
        ratio = 0.0,
        trailerId = "",
        amount = 0.0,
        threshold = 0.0,
        locale = "",
        externalRef = "",
        status = DefaultTransitTimeCheckpointStatus.IDLE,
    )
}
