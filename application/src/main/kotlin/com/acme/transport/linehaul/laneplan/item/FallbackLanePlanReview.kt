package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback lane plan review is in its life cycle. */
enum class FallbackLanePlanReviewStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fallback lane plan review. Its code is unique within a tenant. */
data class FallbackLanePlanReview(
    var code: String,
    var label: String,
    var active: Boolean,
    var currency: String,
    var priority: Int,
    var timezone: String,
    var tags: MutableList<String>,
    var archived: Boolean,
    var attempts: Int,
    var driverId: String,
    var status: FallbackLanePlanReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackLanePlanReviewStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackLanePlanReviewStatus): Boolean {
        when (status) {
            FallbackLanePlanReviewStatus.NEW -> return next == FallbackLanePlanReviewStatus.QUEUED || next == FallbackLanePlanReviewStatus.FAILED
            FallbackLanePlanReviewStatus.QUEUED -> {
                when (next) {
                    FallbackLanePlanReviewStatus.FAILED -> return true
                }
                return false
            }
            FallbackLanePlanReviewStatus.RUNNING -> return next == FallbackLanePlanReviewStatus.DONE
            FallbackLanePlanReviewStatus.DONE -> return next == FallbackLanePlanReviewStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FallbackLanePlanReviewStatus) {
        if (!canMoveTo(next)) {
            error("fallback lane plan review $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveValue(): Double = round((priority.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): FallbackLanePlanReview {
        return FallbackLanePlanReview(
            code = code,
            label = label,
            active = active,
            currency = currency,
            priority = priority,
            timezone = timezone,
            tags = tags,
            archived = archived,
            attempts = attempts,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback lane plan review in status new. */
fun newFallbackLanePlanReview(code: String, label: String): FallbackLanePlanReview {
    return FallbackLanePlanReview(
        code = code,
        label = label,
        active = false,
        currency = "",
        priority = 0,
        timezone = "",
        tags = mutableListOf<String>(),
        archived = false,
        attempts = 0,
        driverId = "",
        status = FallbackLanePlanReviewStatus.NEW,
    )
}
