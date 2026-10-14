package com.acme.transport.network.servicearea.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy service area score records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a legacy service area score is in its life cycle. */
enum class LegacyServiceAreaScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One legacy service area score, with what the planners need to act on it. */
data class LegacyServiceAreaScore(
    var code: String,
    var label: String,
    var active: Boolean,
    var distanceKm: Double,
    var batchSize: Int,
    var etaMinutes: Int,
    var dock: String,
    var status: LegacyServiceAreaScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyServiceAreaScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyServiceAreaScoreStatus): Boolean {
        when (status) {
            LegacyServiceAreaScoreStatus.NEW -> return next == LegacyServiceAreaScoreStatus.RUNNING
            LegacyServiceAreaScoreStatus.QUEUED -> return next == LegacyServiceAreaScoreStatus.FAILED
            LegacyServiceAreaScoreStatus.RUNNING -> return next == LegacyServiceAreaScoreStatus.DONE || next == LegacyServiceAreaScoreStatus.FAILED
            LegacyServiceAreaScoreStatus.DONE -> return next == LegacyServiceAreaScoreStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LegacyServiceAreaScoreStatus) {
        if (!canMoveTo(next)) {
            error("legacy service area score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedExposure(): Double = round((batchSize.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): LegacyServiceAreaScore {
        return LegacyServiceAreaScore(
            code = code,
            label = label,
            active = active,
            distanceKm = distanceKm,
            batchSize = batchSize,
            etaMinutes = etaMinutes,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy service area score in status new. */
fun newLegacyServiceAreaScore(code: String, label: String): LegacyServiceAreaScore {
    return LegacyServiceAreaScore(
        code = code,
        label = label,
        active = false,
        distanceKm = 0.0,
        batchSize = 0,
        etaMinutes = 0,
        dock = "",
        status = LegacyServiceAreaScoreStatus.NEW,
    )
}
