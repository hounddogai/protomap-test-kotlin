package com.acme.transport.emissions.routeoffset.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive route offset score is in its life cycle. */
enum class AdaptiveRouteOffsetScoreStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A adaptive route offset score as the route offset team keeps it. */
data class AdaptiveRouteOffsetScore(
    var code: String,
    var label: String,
    var archived: Boolean,
    var score: Double,
    var name: String,
    var summary: String,
    var reviewer: String,
    var checksum: String,
    var attempts: Int,
    var status: AdaptiveRouteOffsetScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveRouteOffsetScoreStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveRouteOffsetScoreStatus): Boolean {
        when (status) {
            AdaptiveRouteOffsetScoreStatus.CREATED -> return next == AdaptiveRouteOffsetScoreStatus.PUBLISHED || next == AdaptiveRouteOffsetScoreStatus.RETIRED
            AdaptiveRouteOffsetScoreStatus.VALIDATED -> return next == AdaptiveRouteOffsetScoreStatus.PUBLISHED || next == AdaptiveRouteOffsetScoreStatus.RETIRED
            AdaptiveRouteOffsetScoreStatus.PUBLISHED -> {
                when (next) {
                    AdaptiveRouteOffsetScoreStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveRouteOffsetScoreStatus) {
        if (!canMoveTo(next)) {
            error("adaptive route offset score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive route offset score"} $code"

    fun projectedValue(): Double = round(attempts.toDouble() * score * 100) / 100

    fun snapshot(): AdaptiveRouteOffsetScore {
        return AdaptiveRouteOffsetScore(
            code = code,
            label = label,
            archived = archived,
            score = score,
            name = name,
            summary = summary,
            reviewer = reviewer,
            checksum = checksum,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive route offset score in status created. */
fun newAdaptiveRouteOffsetScore(code: String, label: String): AdaptiveRouteOffsetScore {
    return AdaptiveRouteOffsetScore(
        code = code,
        label = label,
        archived = false,
        score = 0.0,
        name = "",
        summary = "",
        reviewer = "",
        checksum = "",
        attempts = 0,
        status = AdaptiveRouteOffsetScoreStatus.CREATED,
    )
}
