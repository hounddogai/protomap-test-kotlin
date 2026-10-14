package com.acme.transport.linehaul.tender.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender review is in its life cycle. */
enum class TenderReviewStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a tender review and its current status. */
data class TenderReview(
    var code: String,
    var title: String,
    var reviewer: String,
    var summary: String,
    var active: Boolean,
    var segment: String,
    var source: String,
    var revision: Int,
    var unit: String,
    var priority: Int,
    var status: TenderReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderReviewStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderReviewStatus): Boolean {
        when (status) {
            TenderReviewStatus.REQUESTED -> return next == TenderReviewStatus.REVOKED || next == TenderReviewStatus.EXPIRED
            TenderReviewStatus.GRANTED -> {
                when (next) {
                    TenderReviewStatus.EXPIRED -> return true
                }
                return false
            }
            TenderReviewStatus.REVOKED -> {
                when (next) {
                    TenderReviewStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderReviewStatus) {
        if (!canMoveTo(next)) {
            error("tender review $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun projectedValue(): Double = round((revision.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): TenderReview {
        return TenderReview(
            code = code,
            title = title,
            reviewer = reviewer,
            summary = summary,
            active = active,
            segment = segment,
            source = source,
            revision = revision,
            unit = unit,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tender review in status requested. */
fun newTenderReview(code: String, title: String): TenderReview {
    return TenderReview(
        code = code,
        title = title,
        reviewer = "",
        summary = "",
        active = false,
        segment = "",
        source = "",
        revision = 0,
        unit = "",
        priority = 0,
        status = TenderReviewStatus.REQUESTED,
    )
}
