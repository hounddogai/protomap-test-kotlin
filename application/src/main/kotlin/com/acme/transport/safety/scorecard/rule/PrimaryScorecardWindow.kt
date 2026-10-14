package com.acme.transport.safety.scorecard.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary scorecard window is in its life cycle. */
enum class PrimaryScorecardWindowStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a primary scorecard window and its current status. */
data class PrimaryScorecardWindow(
    var code: String,
    var label: String,
    var priority: Int,
    var batchSize: Int,
    var stopCount: Int,
    var budget: Double,
    var category: String,
    var country: String,
    var region: String,
    var contactEmail: String,
    var updatedAt: Int,
    var status: PrimaryScorecardWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryScorecardWindowStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryScorecardWindowStatus): Boolean {
        when (status) {
            PrimaryScorecardWindowStatus.OPEN -> return next == PrimaryScorecardWindowStatus.ARCHIVED
            PrimaryScorecardWindowStatus.IN_REVIEW -> {
                when (next) {
                    PrimaryScorecardWindowStatus.RESOLVED, PrimaryScorecardWindowStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryScorecardWindowStatus.RESOLVED -> return next == PrimaryScorecardWindowStatus.ARCHIVED
            PrimaryScorecardWindowStatus.REOPENED -> return next == PrimaryScorecardWindowStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PrimaryScorecardWindowStatus) {
        if (!canMoveTo(next)) {
            error("primary scorecard window $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun netValue(): Double = round((batchSize.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): PrimaryScorecardWindow {
        return PrimaryScorecardWindow(
            code = code,
            label = label,
            priority = priority,
            batchSize = batchSize,
            stopCount = stopCount,
            budget = budget,
            category = category,
            country = country,
            region = region,
            contactEmail = contactEmail,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a primary scorecard window in status open. */
fun newPrimaryScorecardWindow(code: String, label: String): PrimaryScorecardWindow {
    return PrimaryScorecardWindow(
        code = code,
        label = label,
        priority = 0,
        batchSize = 0,
        stopCount = 0,
        budget = 0.0,
        category = "",
        country = "",
        region = "",
        contactEmail = "",
        updatedAt = 0,
        status = PrimaryScorecardWindowStatus.OPEN,
    )
}
