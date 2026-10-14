package com.acme.transport.network.zone.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy zone score is in its life cycle. */
enum class LegacyZoneScoreStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a legacy zone score and its current status. */
data class LegacyZoneScore(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var loadKg: Double,
    var driverId: String,
    var status: LegacyZoneScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyZoneScoreStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyZoneScoreStatus): Boolean {
        when (status) {
            LegacyZoneScoreStatus.PROPOSED -> return next == LegacyZoneScoreStatus.APPROVED || next == LegacyZoneScoreStatus.WITHDRAWN
            LegacyZoneScoreStatus.APPROVED -> return next == LegacyZoneScoreStatus.REJECTED
            LegacyZoneScoreStatus.REJECTED -> return next == LegacyZoneScoreStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyZoneScoreStatus) {
        if (!canMoveTo(next)) {
            error("legacy zone score $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun projectedLoad(): Double = round((updatedAt.toDouble() - loadKg) * 100) / 100

    fun snapshot(): LegacyZoneScore {
        return LegacyZoneScore(
            code = code,
            title = title,
            updatedAt = updatedAt,
            loadKg = loadKg,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy zone score in status proposed. */
fun newLegacyZoneScore(code: String, title: String): LegacyZoneScore {
    return LegacyZoneScore(
        code = code,
        title = title,
        updatedAt = 0,
        loadKg = 0.0,
        driverId = "",
        status = LegacyZoneScoreStatus.PROPOSED,
    )
}
