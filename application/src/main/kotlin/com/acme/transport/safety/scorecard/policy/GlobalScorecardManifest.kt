package com.acme.transport.safety.scorecard.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global scorecard manifest is in its life cycle. */
enum class GlobalScorecardManifestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A global scorecard manifest as the scorecard team keeps it. */
data class GlobalScorecardManifest(
    var code: String,
    var name: String,
    var channel: String,
    var limit: Int,
    var description: String,
    var durationMinutes: Int,
    var title: String,
    var odometer: Int,
    var startsAt: Int,
    var status: GlobalScorecardManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalScorecardManifestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalScorecardManifestStatus): Boolean {
        when (status) {
            GlobalScorecardManifestStatus.PLANNED -> {
                when (next) {
                    GlobalScorecardManifestStatus.IN_PROGRESS, GlobalScorecardManifestStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalScorecardManifestStatus.SCHEDULED -> {
                when (next) {
                    GlobalScorecardManifestStatus.IN_PROGRESS, GlobalScorecardManifestStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalScorecardManifestStatus.IN_PROGRESS -> return next == GlobalScorecardManifestStatus.COMPLETED
            GlobalScorecardManifestStatus.COMPLETED -> return next == GlobalScorecardManifestStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: GlobalScorecardManifestStatus) {
        if (!canMoveTo(next)) {
            error("global scorecard manifest $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun netValue(): Double = round((durationMinutes.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): GlobalScorecardManifest {
        return GlobalScorecardManifest(
            code = code,
            name = name,
            channel = channel,
            limit = limit,
            description = description,
            durationMinutes = durationMinutes,
            title = title,
            odometer = odometer,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a global scorecard manifest in status planned. */
fun newGlobalScorecardManifest(code: String, name: String): GlobalScorecardManifest {
    return GlobalScorecardManifest(
        code = code,
        name = name,
        channel = "",
        limit = 0,
        description = "",
        durationMinutes = 0,
        title = "",
        odometer = 0,
        startsAt = 0,
        status = GlobalScorecardManifestStatus.PLANNED,
    )
}
