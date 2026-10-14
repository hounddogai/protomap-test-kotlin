package com.acme.transport.safety.scorecard.scorecardprofile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling scorecard profile records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a rolling scorecard profile is in its life cycle. */
enum class RollingScorecardProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A rolling scorecard profile. Its code is unique within a tenant. */
data class RollingScorecardProfile(
    var code: String,
    var label: String,
    var shiftCode: String,
    var stopCount: Int,
    var score: Double,
    var quantity: Int,
    var tags: MutableList<String>,
    var locale: String,
    var distanceKm: Double,
    var loadKg: Double,
    var dock: String,
    var status: RollingScorecardProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingScorecardProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingScorecardProfileStatus): Boolean {
        when (status) {
            RollingScorecardProfileStatus.PLANNED -> return next == RollingScorecardProfileStatus.IN_PROGRESS
            RollingScorecardProfileStatus.SCHEDULED -> return next == RollingScorecardProfileStatus.IN_PROGRESS || next == RollingScorecardProfileStatus.CANCELLED
            RollingScorecardProfileStatus.IN_PROGRESS -> return next == RollingScorecardProfileStatus.CANCELLED
            RollingScorecardProfileStatus.COMPLETED -> return next == RollingScorecardProfileStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RollingScorecardProfileStatus) {
        if (!canMoveTo(next)) {
            error("rolling scorecard profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rolling scorecard profile"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedVolume(): Double = round((stopCount.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): RollingScorecardProfile {
        return RollingScorecardProfile(
            code = code,
            label = label,
            shiftCode = shiftCode,
            stopCount = stopCount,
            score = score,
            quantity = quantity,
            tags = tags,
            locale = locale,
            distanceKm = distanceKm,
            loadKg = loadKg,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rolling scorecard profile in status planned. */
fun newRollingScorecardProfile(code: String, label: String): RollingScorecardProfile {
    return RollingScorecardProfile(
        code = code,
        label = label,
        shiftCode = "",
        stopCount = 0,
        score = 0.0,
        quantity = 0,
        tags = mutableListOf<String>(),
        locale = "",
        distanceKm = 0.0,
        loadKg = 0.0,
        dock = "",
        status = RollingScorecardProfileStatus.PLANNED,
    )
}
