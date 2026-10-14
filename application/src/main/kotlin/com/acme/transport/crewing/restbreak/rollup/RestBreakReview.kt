package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break review is in its life cycle. */
enum class RestBreakReviewStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rest break review. Its code is unique within a tenant. */
data class RestBreakReview(
    var code: String,
    var title: String,
    var active: Boolean,
    var version: Int,
    var maximum: Double,
    var odometer: Int,
    var tags: MutableList<String>,
    var status: RestBreakReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakReviewStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakReviewStatus): Boolean {
        when (status) {
            RestBreakReviewStatus.CREATED -> return next == RestBreakReviewStatus.PUBLISHED
            RestBreakReviewStatus.VALIDATED -> return next == RestBreakReviewStatus.RETIRED
            RestBreakReviewStatus.PUBLISHED -> {
                when (next) {
                    RestBreakReviewStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakReviewStatus) {
        if (!canMoveTo(next)) {
            error("rest break review $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"rest break review"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedExposure(): Double = round((version.toDouble() - maximum) * 100) / 100

    fun snapshot(): RestBreakReview {
        return RestBreakReview(
            code = code,
            title = title,
            active = active,
            version = version,
            maximum = maximum,
            odometer = odometer,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rest break review in status created. */
fun newRestBreakReview(code: String, title: String): RestBreakReview {
    return RestBreakReview(
        code = code,
        title = title,
        active = false,
        version = 0,
        maximum = 0.0,
        odometer = 0,
        tags = mutableListOf<String>(),
        status = RestBreakReviewStatus.CREATED,
    )
}
