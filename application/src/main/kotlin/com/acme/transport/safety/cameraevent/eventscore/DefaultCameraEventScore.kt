package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default camera event score records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a default camera event score is in its life cycle. */
enum class DefaultCameraEventScoreStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One default camera event score, with what the planners need to act on it. */
data class DefaultCameraEventScore(
    var code: String,
    var label: String,
    var title: String,
    var routeCode: String,
    var priority: Int,
    var notes: String,
    var stopCount: Int,
    var rate: Double,
    var distanceKm: Double,
    var limit: Int,
    var startsAt: Int,
    var status: DefaultCameraEventScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCameraEventScoreStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCameraEventScoreStatus): Boolean {
        when (status) {
            DefaultCameraEventScoreStatus.PROPOSED -> {
                when (next) {
                    DefaultCameraEventScoreStatus.APPROVED, DefaultCameraEventScoreStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefaultCameraEventScoreStatus.APPROVED -> {
                when (next) {
                    DefaultCameraEventScoreStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefaultCameraEventScoreStatus.REJECTED -> {
                when (next) {
                    DefaultCameraEventScoreStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultCameraEventScoreStatus) {
        if (!canMoveTo(next)) {
            error("default camera event score $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveExposure(): Double = round((startsAt.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): DefaultCameraEventScore {
        return DefaultCameraEventScore(
            code = code,
            label = label,
            title = title,
            routeCode = routeCode,
            priority = priority,
            notes = notes,
            stopCount = stopCount,
            rate = rate,
            distanceKm = distanceKm,
            limit = limit,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default camera event score in status proposed. */
fun newDefaultCameraEventScore(code: String, label: String): DefaultCameraEventScore {
    return DefaultCameraEventScore(
        code = code,
        label = label,
        title = "",
        routeCode = "",
        priority = 0,
        notes = "",
        stopCount = 0,
        rate = 0.0,
        distanceKm = 0.0,
        limit = 0,
        startsAt = 0,
        status = DefaultCameraEventScoreStatus.PROPOSED,
    )
}
