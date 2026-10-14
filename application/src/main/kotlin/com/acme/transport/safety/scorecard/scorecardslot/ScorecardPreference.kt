package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard preference is in its life cycle. */
enum class ScorecardPreferenceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A scorecard preference. Its code is unique within a tenant. */
data class ScorecardPreference(
    var code: String,
    var title: String,
    var capacity: Int,
    var weight: Double,
    var revision: Int,
    var batchSize: Int,
    var loadKg: Double,
    var summary: String,
    var updatedAt: Int,
    var locale: String,
    var reviewer: String,
    var status: ScorecardPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardPreferenceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardPreferenceStatus): Boolean {
        when (status) {
            ScorecardPreferenceStatus.CREATED -> return next == ScorecardPreferenceStatus.VALIDATED
            ScorecardPreferenceStatus.VALIDATED -> {
                when (next) {
                    ScorecardPreferenceStatus.PUBLISHED -> return true
                }
                return false
            }
            ScorecardPreferenceStatus.PUBLISHED -> return next == ScorecardPreferenceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScorecardPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("scorecard preference $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun netVolume(): Double = round((capacity.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): ScorecardPreference {
        return ScorecardPreference(
            code = code,
            title = title,
            capacity = capacity,
            weight = weight,
            revision = revision,
            batchSize = batchSize,
            loadKg = loadKg,
            summary = summary,
            updatedAt = updatedAt,
            locale = locale,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a scorecard preference in status created. */
fun newScorecardPreference(code: String, title: String): ScorecardPreference {
    return ScorecardPreference(
        code = code,
        title = title,
        capacity = 0,
        weight = 0.0,
        revision = 0,
        batchSize = 0,
        loadKg = 0.0,
        summary = "",
        updatedAt = 0,
        locale = "",
        reviewer = "",
        status = ScorecardPreferenceStatus.CREATED,
    )
}
