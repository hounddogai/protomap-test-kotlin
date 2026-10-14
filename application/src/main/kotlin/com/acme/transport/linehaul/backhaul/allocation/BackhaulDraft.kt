package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul draft is in its life cycle. */
enum class BackhaulDraftStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One backhaul draft, with what the planners need to act on it. */
data class BackhaulDraft(
    var code: String,
    var label: String,
    var trailerId: String,
    var loadKg: Double,
    var discount: Double,
    var distanceKm: Double,
    var minimum: Double,
    var score: Double,
    var expiresAt: Int,
    var durationMinutes: Int,
    var status: BackhaulDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulDraftStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulDraftStatus): Boolean {
        when (status) {
            BackhaulDraftStatus.PLANNED -> return next == BackhaulDraftStatus.CANCELLED
            BackhaulDraftStatus.SCHEDULED -> return next == BackhaulDraftStatus.CANCELLED
            BackhaulDraftStatus.IN_PROGRESS -> return next == BackhaulDraftStatus.CANCELLED
            BackhaulDraftStatus.COMPLETED -> return next == BackhaulDraftStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BackhaulDraftStatus) {
        if (!canMoveTo(next)) {
            error("backhaul draft $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun netExposure(): Double = round(score * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): BackhaulDraft {
        return BackhaulDraft(
            code = code,
            label = label,
            trailerId = trailerId,
            loadKg = loadKg,
            discount = discount,
            distanceKm = distanceKm,
            minimum = minimum,
            score = score,
            expiresAt = expiresAt,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a backhaul draft in status planned. */
fun newBackhaulDraft(code: String, label: String): BackhaulDraft {
    return BackhaulDraft(
        code = code,
        label = label,
        trailerId = "",
        loadKg = 0.0,
        discount = 0.0,
        distanceKm = 0.0,
        minimum = 0.0,
        score = 0.0,
        expiresAt = 0,
        durationMinutes = 0,
        status = BackhaulDraftStatus.PLANNED,
    )
}
