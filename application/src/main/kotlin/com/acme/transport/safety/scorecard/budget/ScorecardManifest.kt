package com.acme.transport.safety.scorecard.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard manifest is in its life cycle. */
enum class ScorecardManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scorecard manifest as the scorecard team keeps it. */
data class ScorecardManifest(
    var code: String,
    var title: String,
    var odometer: Int,
    var minimum: Double,
    var notes: String,
    var rate: Double,
    var archived: Boolean,
    var status: ScorecardManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardManifestStatus): Boolean {
        when (status) {
            ScorecardManifestStatus.NEW -> return next == ScorecardManifestStatus.FAILED
            ScorecardManifestStatus.QUEUED -> return next == ScorecardManifestStatus.DONE
            ScorecardManifestStatus.RUNNING -> {
                when (next) {
                    ScorecardManifestStatus.DONE, ScorecardManifestStatus.FAILED -> return true
                }
                return false
            }
            ScorecardManifestStatus.DONE -> return next == ScorecardManifestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScorecardManifestStatus) {
        if (!canMoveTo(next)) {
            error("scorecard manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedTotal(): Double = round((odometer.toDouble() + minimum) * 100) / 100

    fun snapshot(): ScorecardManifest {
        return ScorecardManifest(
            code = code,
            title = title,
            odometer = odometer,
            minimum = minimum,
            notes = notes,
            rate = rate,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scorecard manifest in status new. */
fun newScorecardManifest(code: String, title: String): ScorecardManifest {
    return ScorecardManifest(
        code = code,
        title = title,
        odometer = 0,
        minimum = 0.0,
        notes = "",
        rate = 0.0,
        archived = false,
        status = ScorecardManifestStatus.NEW,
    )
}
