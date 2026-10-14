package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback backhaul request is in its life cycle. */
enum class FallbackBackhaulRequestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One fallback backhaul request, with what the planners need to act on it. */
data class FallbackBackhaulRequest(
    var code: String,
    var title: String,
    var channel: String,
    var score: Double,
    var contactEmail: String,
    var windowDays: Int,
    var visible: Boolean,
    var updatedAt: Int,
    var margin: Double,
    var trailerId: String,
    var status: FallbackBackhaulRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackBackhaulRequestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackBackhaulRequestStatus): Boolean {
        when (status) {
            FallbackBackhaulRequestStatus.IDLE -> {
                when (next) {
                    FallbackBackhaulRequestStatus.STOPPED -> return true
                }
                return false
            }
            FallbackBackhaulRequestStatus.WARMING -> {
                when (next) {
                    FallbackBackhaulRequestStatus.READY, FallbackBackhaulRequestStatus.STOPPED -> return true
                }
                return false
            }
            FallbackBackhaulRequestStatus.READY -> return next == FallbackBackhaulRequestStatus.STOPPED
            FallbackBackhaulRequestStatus.DRAINING -> {
                when (next) {
                    FallbackBackhaulRequestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackBackhaulRequestStatus) {
        if (!canMoveTo(next)) {
            error("fallback backhaul request $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun adjustedExposure(): Double = round(updatedAt.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): FallbackBackhaulRequest {
        return FallbackBackhaulRequest(
            code = code,
            title = title,
            channel = channel,
            score = score,
            contactEmail = contactEmail,
            windowDays = windowDays,
            visible = visible,
            updatedAt = updatedAt,
            margin = margin,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a fallback backhaul request in status idle. */
fun newFallbackBackhaulRequest(code: String, title: String): FallbackBackhaulRequest {
    return FallbackBackhaulRequest(
        code = code,
        title = title,
        channel = "",
        score = 0.0,
        contactEmail = "",
        windowDays = 0,
        visible = false,
        updatedAt = 0,
        margin = 0.0,
        trailerId = "",
        status = FallbackBackhaulRequestStatus.IDLE,
    )
}
