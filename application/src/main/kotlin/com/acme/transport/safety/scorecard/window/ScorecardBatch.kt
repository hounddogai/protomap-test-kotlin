package com.acme.transport.safety.scorecard.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard batch is in its life cycle. */
enum class ScorecardBatchStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A scorecard batch. Its code is unique within a tenant. */
data class ScorecardBatch(
    var code: String,
    var title: String,
    var loadKg: Double,
    var endsAt: Int,
    var expiresAt: Int,
    var windowDays: Int,
    var approved: Boolean,
    var label: String,
    var depot: String,
    var status: ScorecardBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardBatchStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardBatchStatus): Boolean {
        when (status) {
            ScorecardBatchStatus.PLANNED -> {
                when (next) {
                    ScorecardBatchStatus.SCHEDULED -> return true
                }
                return false
            }
            ScorecardBatchStatus.SCHEDULED -> {
                when (next) {
                    ScorecardBatchStatus.COMPLETED -> return true
                }
                return false
            }
            ScorecardBatchStatus.IN_PROGRESS -> {
                when (next) {
                    ScorecardBatchStatus.COMPLETED -> return true
                }
                return false
            }
            ScorecardBatchStatus.COMPLETED -> {
                when (next) {
                    ScorecardBatchStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScorecardBatchStatus) {
        if (!canMoveTo(next)) {
            error("scorecard batch $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $title"

    fun netValue(): Double = round((expiresAt.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): ScorecardBatch {
        return ScorecardBatch(
            code = code,
            title = title,
            loadKg = loadKg,
            endsAt = endsAt,
            expiresAt = expiresAt,
            windowDays = windowDays,
            approved = approved,
            label = label,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scorecard batch in status planned. */
fun newScorecardBatch(code: String, title: String): ScorecardBatch {
    return ScorecardBatch(
        code = code,
        title = title,
        loadKg = 0.0,
        endsAt = 0,
        expiresAt = 0,
        windowDays = 0,
        approved = false,
        label = "",
        depot = "",
        status = ScorecardBatchStatus.PLANNED,
    )
}
