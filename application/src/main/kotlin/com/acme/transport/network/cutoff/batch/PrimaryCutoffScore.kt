package com.acme.transport.network.cutoff.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary cutoff score is in its life cycle. */
enum class PrimaryCutoffScoreStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A primary cutoff score as the cutoff team keeps it. */
data class PrimaryCutoffScore(
    var code: String,
    var label: String,
    var visible: Boolean,
    var minimum: Double,
    var unit: String,
    var contactEmail: String,
    var segment: String,
    var depot: String,
    var margin: Double,
    var archived: Boolean,
    var status: PrimaryCutoffScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCutoffScoreStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCutoffScoreStatus): Boolean {
        when (status) {
            PrimaryCutoffScoreStatus.CREATED -> {
                when (next) {
                    PrimaryCutoffScoreStatus.VALIDATED -> return true
                }
                return false
            }
            PrimaryCutoffScoreStatus.VALIDATED -> return next == PrimaryCutoffScoreStatus.PUBLISHED || next == PrimaryCutoffScoreStatus.RETIRED
            PrimaryCutoffScoreStatus.PUBLISHED -> return next == PrimaryCutoffScoreStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PrimaryCutoffScoreStatus) {
        if (!canMoveTo(next)) {
            error("primary cutoff score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary cutoff score"} $code"

    fun adjustedExposure(): Double = round(minimum * margin * 100) / 100

    fun snapshot(): PrimaryCutoffScore {
        return PrimaryCutoffScore(
            code = code,
            label = label,
            visible = visible,
            minimum = minimum,
            unit = unit,
            contactEmail = contactEmail,
            segment = segment,
            depot = depot,
            margin = margin,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a primary cutoff score in status created. */
fun newPrimaryCutoffScore(code: String, label: String): PrimaryCutoffScore {
    return PrimaryCutoffScore(
        code = code,
        label = label,
        visible = false,
        minimum = 0.0,
        unit = "",
        contactEmail = "",
        segment = "",
        depot = "",
        margin = 0.0,
        archived = false,
        status = PrimaryCutoffScoreStatus.CREATED,
    )
}
