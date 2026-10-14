package com.acme.transport.safety.scorecard.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual scorecard template is in its life cycle. */
enum class ManualScorecardTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A manual scorecard template. Its code is unique within a tenant. */
data class ManualScorecardTemplate(
    var code: String,
    var title: String,
    var depot: String,
    var distanceKm: Double,
    var label: String,
    var minimum: Double,
    var pinned: Boolean,
    var status: ManualScorecardTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualScorecardTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualScorecardTemplateStatus): Boolean {
        when (status) {
            ManualScorecardTemplateStatus.DRAFT -> return next == ManualScorecardTemplateStatus.ACTIVE
            ManualScorecardTemplateStatus.PENDING -> {
                when (next) {
                    ManualScorecardTemplateStatus.ACTIVE, ManualScorecardTemplateStatus.CLOSED -> return true
                }
                return false
            }
            ManualScorecardTemplateStatus.ACTIVE -> return next == ManualScorecardTemplateStatus.SUSPENDED || next == ManualScorecardTemplateStatus.CLOSED
            ManualScorecardTemplateStatus.SUSPENDED -> {
                when (next) {
                    ManualScorecardTemplateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualScorecardTemplateStatus) {
        if (!canMoveTo(next)) {
            error("manual scorecard template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun netLoad(): Double = round((minimum + distanceKm) * 100) / 100

    fun snapshot(): ManualScorecardTemplate {
        return ManualScorecardTemplate(
            code = code,
            title = title,
            depot = depot,
            distanceKm = distanceKm,
            label = label,
            minimum = minimum,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a manual scorecard template in status draft. */
fun newManualScorecardTemplate(code: String, title: String): ManualScorecardTemplate {
    return ManualScorecardTemplate(
        code = code,
        title = title,
        depot = "",
        distanceKm = 0.0,
        label = "",
        minimum = 0.0,
        pinned = false,
        status = ManualScorecardTemplateStatus.DRAFT,
    )
}
