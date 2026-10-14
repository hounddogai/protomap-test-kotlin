package com.acme.transport.lastmile.parcel.review

import com.acme.transport.kit.text.normalize

// The parcel review records of lastmile's parcel work, and the rules, stores, and calculations around them.

/** Where a parcel review is in its life cycle. */
enum class ParcelReviewStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A parcel review. Its code is unique within a tenant. */
data class ParcelReview(
    var code: String,
    var title: String,
    var visible: Boolean,
    var createdAt: Int,
    var trailerId: String,
    var contactEmail: String,
    var status: ParcelReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelReviewStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelReviewStatus): Boolean {
        when (status) {
            ParcelReviewStatus.CREATED -> return next == ParcelReviewStatus.VALIDATED || next == ParcelReviewStatus.PUBLISHED
            ParcelReviewStatus.VALIDATED -> {
                when (next) {
                    ParcelReviewStatus.PUBLISHED -> return true
                }
                return false
            }
            ParcelReviewStatus.PUBLISHED -> return next == ParcelReviewStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ParcelReviewStatus) {
        if (!canMoveTo(next)) {
            error("parcel review $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): ParcelReview {
        return ParcelReview(
            code = code,
            title = title,
            visible = visible,
            createdAt = createdAt,
            trailerId = trailerId,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a parcel review in status created. */
fun newParcelReview(code: String, title: String): ParcelReview {
    return ParcelReview(
        code = code,
        title = title,
        visible = false,
        createdAt = 0,
        trailerId = "",
        contactEmail = "",
        status = ParcelReviewStatus.CREATED,
    )
}
