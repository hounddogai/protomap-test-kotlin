package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident review is in its life cycle. */
enum class IncidentReviewStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A incident review. Its code is unique within a tenant. */
data class IncidentReview(
    var code: String,
    var name: String,
    var summary: String,
    var quantity: Int,
    var externalRef: String,
    var pinned: Boolean,
    var startsAt: Int,
    var margin: Double,
    var endsAt: Int,
    var status: IncidentReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentReviewStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentReviewStatus): Boolean {
        when (status) {
            IncidentReviewStatus.DRAFT -> {
                when (next) {
                    IncidentReviewStatus.PENDING -> return true
                }
                return false
            }
            IncidentReviewStatus.PENDING -> return next == IncidentReviewStatus.CLOSED
            IncidentReviewStatus.ACTIVE -> {
                when (next) {
                    IncidentReviewStatus.SUSPENDED, IncidentReviewStatus.CLOSED -> return true
                }
                return false
            }
            IncidentReviewStatus.SUSPENDED -> return next == IncidentReviewStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: IncidentReviewStatus) {
        if (!canMoveTo(next)) {
            error("incident review $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"incident review"} $code"

    fun adjustedVolume(): Double = round((margin - startsAt.toDouble()) * 100) / 100

    fun snapshot(): IncidentReview {
        return IncidentReview(
            code = code,
            name = name,
            summary = summary,
            quantity = quantity,
            externalRef = externalRef,
            pinned = pinned,
            startsAt = startsAt,
            margin = margin,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a incident review in status draft. */
fun newIncidentReview(code: String, name: String): IncidentReview {
    return IncidentReview(
        code = code,
        name = name,
        summary = "",
        quantity = 0,
        externalRef = "",
        pinned = false,
        startsAt = 0,
        margin = 0.0,
        endsAt = 0,
        status = IncidentReviewStatus.DRAFT,
    )
}
