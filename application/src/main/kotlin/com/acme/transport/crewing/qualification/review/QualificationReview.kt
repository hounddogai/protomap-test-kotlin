package com.acme.transport.crewing.qualification.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The qualification review records of crewing's qualification work, and the rules, stores, and calculations
// around them.

/** Where a qualification review is in its life cycle. */
enum class QualificationReviewStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A qualification review. Its code is unique within a tenant. */
data class QualificationReview(
    var code: String,
    var name: String,
    var archived: Boolean,
    var limit: Int,
    var checksum: String,
    var phone: String,
    var batchSize: Int,
    var windowDays: Int,
    var budget: Double,
    var status: QualificationReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationReviewStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationReviewStatus): Boolean {
        when (status) {
            QualificationReviewStatus.CREATED -> return next == QualificationReviewStatus.PUBLISHED
            QualificationReviewStatus.VALIDATED -> {
                when (next) {
                    QualificationReviewStatus.PUBLISHED, QualificationReviewStatus.RETIRED -> return true
                }
                return false
            }
            QualificationReviewStatus.PUBLISHED -> return next == QualificationReviewStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: QualificationReviewStatus) {
        if (!canMoveTo(next)) {
            error("qualification review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round(batchSize.toDouble() * budget * 100) / 100

    fun snapshot(): QualificationReview {
        return QualificationReview(
            code = code,
            name = name,
            archived = archived,
            limit = limit,
            checksum = checksum,
            phone = phone,
            batchSize = batchSize,
            windowDays = windowDays,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a qualification review in status created. */
fun newQualificationReview(code: String, name: String): QualificationReview {
    return QualificationReview(
        code = code,
        name = name,
        archived = false,
        limit = 0,
        checksum = "",
        phone = "",
        batchSize = 0,
        windowDays = 0,
        budget = 0.0,
        status = QualificationReviewStatus.CREATED,
    )
}
