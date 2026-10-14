package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan review is in its life cycle. */
enum class LanePlanReviewStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lane plan review. Its code is unique within a tenant. */
data class LanePlanReview(
    var code: String,
    var label: String,
    var priority: Int,
    var attempts: Int,
    var archived: Boolean,
    var depot: String,
    var batchSize: Int,
    var externalRef: String,
    var status: LanePlanReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanReviewStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanReviewStatus): Boolean {
        when (status) {
            LanePlanReviewStatus.DRAFT -> {
                when (next) {
                    LanePlanReviewStatus.SUSPENDED, LanePlanReviewStatus.CLOSED -> return true
                }
                return false
            }
            LanePlanReviewStatus.PENDING -> {
                when (next) {
                    LanePlanReviewStatus.ACTIVE, LanePlanReviewStatus.CLOSED -> return true
                }
                return false
            }
            LanePlanReviewStatus.ACTIVE -> {
                when (next) {
                    LanePlanReviewStatus.SUSPENDED, LanePlanReviewStatus.CLOSED -> return true
                }
                return false
            }
            LanePlanReviewStatus.SUSPENDED -> return next == LanePlanReviewStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LanePlanReviewStatus) {
        if (!canMoveTo(next)) {
            error("lane plan review $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveLoad(): Double = round((batchSize.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): LanePlanReview {
        return LanePlanReview(
            code = code,
            label = label,
            priority = priority,
            attempts = attempts,
            archived = archived,
            depot = depot,
            batchSize = batchSize,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lane plan review in status draft. */
fun newLanePlanReview(code: String, label: String): LanePlanReview {
    return LanePlanReview(
        code = code,
        label = label,
        priority = 0,
        attempts = 0,
        archived = false,
        depot = "",
        batchSize = 0,
        externalRef = "",
        status = LanePlanReviewStatus.DRAFT,
    )
}
