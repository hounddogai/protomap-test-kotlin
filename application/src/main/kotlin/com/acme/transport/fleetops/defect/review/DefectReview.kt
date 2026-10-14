package com.acme.transport.fleetops.defect.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The defect review records of fleetops's defect work, and the rules, stores, and calculations around them.

/** Where a defect review is in its life cycle. */
enum class DefectReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A defect review as the defect team keeps it. */
data class DefectReview(
    var code: String,
    var name: String,
    var pallets: Int,
    var tags: MutableList<String>,
    var batchSize: Int,
    var graceDays: Int,
    var expiresAt: Int,
    var ceiling: Double,
    var endsAt: Int,
    var archived: Boolean,
    var status: DefectReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectReviewStatus): Boolean {
        when (status) {
            DefectReviewStatus.PROPOSED -> {
                when (next) {
                    DefectReviewStatus.REJECTED -> return true
                }
                return false
            }
            DefectReviewStatus.APPROVED -> return next == DefectReviewStatus.REJECTED || next == DefectReviewStatus.WITHDRAWN
            DefectReviewStatus.REJECTED -> {
                when (next) {
                    DefectReviewStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectReviewStatus) {
        if (!canMoveTo(next)) {
            error("defect review $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedTotal(): Double = round(graceDays.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): DefectReview {
        return DefectReview(
            code = code,
            name = name,
            pallets = pallets,
            tags = tags,
            batchSize = batchSize,
            graceDays = graceDays,
            expiresAt = expiresAt,
            ceiling = ceiling,
            endsAt = endsAt,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a defect review in status proposed. */
fun newDefectReview(code: String, name: String): DefectReview {
    return DefectReview(
        code = code,
        name = name,
        pallets = 0,
        tags = mutableListOf<String>(),
        batchSize = 0,
        graceDays = 0,
        expiresAt = 0,
        ceiling = 0.0,
        endsAt = 0,
        archived = false,
        status = DefectReviewStatus.PROPOSED,
    )
}
