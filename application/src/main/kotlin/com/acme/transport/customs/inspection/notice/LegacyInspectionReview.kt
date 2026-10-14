package com.acme.transport.customs.inspection.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy inspection review is in its life cycle. */
enum class LegacyInspectionReviewStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One legacy inspection review, with what the planners need to act on it. */
data class LegacyInspectionReview(
    var code: String,
    var title: String,
    var revision: Int,
    var dock: String,
    var retries: Int,
    var lane: String,
    var externalRef: String,
    var version: Int,
    var status: LegacyInspectionReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyInspectionReviewStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyInspectionReviewStatus): Boolean {
        when (status) {
            LegacyInspectionReviewStatus.OPEN -> {
                when (next) {
                    LegacyInspectionReviewStatus.REOPENED, LegacyInspectionReviewStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyInspectionReviewStatus.IN_REVIEW -> return next == LegacyInspectionReviewStatus.ARCHIVED
            LegacyInspectionReviewStatus.RESOLVED -> {
                when (next) {
                    LegacyInspectionReviewStatus.REOPENED, LegacyInspectionReviewStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyInspectionReviewStatus.REOPENED -> return next == LegacyInspectionReviewStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LegacyInspectionReviewStatus) {
        if (!canMoveTo(next)) {
            error("legacy inspection review $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun netVolume(): Double = round((version.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): LegacyInspectionReview {
        return LegacyInspectionReview(
            code = code,
            title = title,
            revision = revision,
            dock = dock,
            retries = retries,
            lane = lane,
            externalRef = externalRef,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a legacy inspection review in status open. */
fun newLegacyInspectionReview(code: String, title: String): LegacyInspectionReview {
    return LegacyInspectionReview(
        code = code,
        title = title,
        revision = 0,
        dock = "",
        retries = 0,
        lane = "",
        externalRef = "",
        version = 0,
        status = LegacyInspectionReviewStatus.OPEN,
    )
}
