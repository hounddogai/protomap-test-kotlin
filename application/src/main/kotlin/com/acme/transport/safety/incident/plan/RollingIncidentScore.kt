package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling incident score is in its life cycle. */
enum class RollingIncidentScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A rolling incident score as the incident team keeps it. */
data class RollingIncidentScore(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var loadKg: Double,
    var priority: Int,
    var stopCount: Int,
    var archived: Boolean,
    var routeCode: String,
    var category: String,
    var minimum: Double,
    var status: RollingIncidentScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingIncidentScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingIncidentScoreStatus): Boolean {
        when (status) {
            RollingIncidentScoreStatus.PLANNED -> return next == RollingIncidentScoreStatus.CANCELLED
            RollingIncidentScoreStatus.SCHEDULED -> {
                when (next) {
                    RollingIncidentScoreStatus.IN_PROGRESS -> return true
                }
                return false
            }
            RollingIncidentScoreStatus.IN_PROGRESS -> {
                when (next) {
                    RollingIncidentScoreStatus.CANCELLED -> return true
                }
                return false
            }
            RollingIncidentScoreStatus.COMPLETED -> {
                when (next) {
                    RollingIncidentScoreStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingIncidentScoreStatus) {
        if (!canMoveTo(next)) {
            error("rolling incident score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netVolume(): Double = round((loadKg - stopCount.toDouble()) * 100) / 100

    fun snapshot(): RollingIncidentScore {
        return RollingIncidentScore(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            loadKg = loadKg,
            priority = priority,
            stopCount = stopCount,
            archived = archived,
            routeCode = routeCode,
            category = category,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rolling incident score in status planned. */
fun newRollingIncidentScore(code: String, label: String): RollingIncidentScore {
    return RollingIncidentScore(
        code = code,
        label = label,
        etaMinutes = 0,
        loadKg = 0.0,
        priority = 0,
        stopCount = 0,
        archived = false,
        routeCode = "",
        category = "",
        minimum = 0.0,
        status = RollingIncidentScoreStatus.PLANNED,
    )
}
