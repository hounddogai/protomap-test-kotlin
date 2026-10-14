package com.acme.transport.safety.incident.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal incident claim is in its life cycle. */
enum class SeasonalIncidentClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A seasonal incident claim as the incident team keeps it. */
data class SeasonalIncidentClaim(
    var code: String,
    var label: String,
    var category: String,
    var ratio: Double,
    var title: String,
    var margin: Double,
    var status: SeasonalIncidentClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalIncidentClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalIncidentClaimStatus): Boolean {
        when (status) {
            SeasonalIncidentClaimStatus.OPEN -> {
                when (next) {
                    SeasonalIncidentClaimStatus.IN_REVIEW, SeasonalIncidentClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalIncidentClaimStatus.IN_REVIEW -> return next == SeasonalIncidentClaimStatus.RESOLVED || next == SeasonalIncidentClaimStatus.REOPENED
            SeasonalIncidentClaimStatus.RESOLVED -> return next == SeasonalIncidentClaimStatus.REOPENED
            SeasonalIncidentClaimStatus.REOPENED -> return next == SeasonalIncidentClaimStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SeasonalIncidentClaimStatus) {
        if (!canMoveTo(next)) {
            error("seasonal incident claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedTotal(): Double = round(margin * ratio * 100) / 100

    fun snapshot(): SeasonalIncidentClaim {
        return SeasonalIncidentClaim(
            code = code,
            label = label,
            category = category,
            ratio = ratio,
            title = title,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a seasonal incident claim in status open. */
fun newSeasonalIncidentClaim(code: String, label: String): SeasonalIncidentClaim {
    return SeasonalIncidentClaim(
        code = code,
        label = label,
        category = "",
        ratio = 0.0,
        title = "",
        margin = 0.0,
        status = SeasonalIncidentClaimStatus.OPEN,
    )
}
