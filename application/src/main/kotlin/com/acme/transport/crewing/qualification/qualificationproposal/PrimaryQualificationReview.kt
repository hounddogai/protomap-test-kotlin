package com.acme.transport.crewing.qualification.qualificationproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary qualification review is in its life cycle. */
enum class PrimaryQualificationReviewStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A primary qualification review. Its code is unique within a tenant. */
data class PrimaryQualificationReview(
    var code: String,
    var label: String,
    var attempts: Int,
    var revision: Int,
    var notes: String,
    var lane: String,
    var status: PrimaryQualificationReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryQualificationReviewStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryQualificationReviewStatus): Boolean {
        when (status) {
            PrimaryQualificationReviewStatus.REQUESTED -> return next == PrimaryQualificationReviewStatus.EXPIRED
            PrimaryQualificationReviewStatus.GRANTED -> return next == PrimaryQualificationReviewStatus.REVOKED
            PrimaryQualificationReviewStatus.REVOKED -> return next == PrimaryQualificationReviewStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PrimaryQualificationReviewStatus) {
        if (!canMoveTo(next)) {
            error("primary qualification review $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun netExposure(): Double = round(revision.toDouble() * attempts.toDouble() * 100) / 100

    fun snapshot(): PrimaryQualificationReview {
        return PrimaryQualificationReview(
            code = code,
            label = label,
            attempts = attempts,
            revision = revision,
            notes = notes,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a primary qualification review in status requested. */
fun newPrimaryQualificationReview(code: String, label: String): PrimaryQualificationReview {
    return PrimaryQualificationReview(
        code = code,
        label = label,
        attempts = 0,
        revision = 0,
        notes = "",
        lane = "",
        status = PrimaryQualificationReviewStatus.REQUESTED,
    )
}
