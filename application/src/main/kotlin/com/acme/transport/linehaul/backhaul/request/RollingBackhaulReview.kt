package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling backhaul review is in its life cycle. */
enum class RollingBackhaulReviewStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One rolling backhaul review, with what the planners need to act on it. */
data class RollingBackhaulReview(
    var code: String,
    var name: String,
    var createdAt: Int,
    var category: String,
    var version: Int,
    var status: RollingBackhaulReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingBackhaulReviewStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingBackhaulReviewStatus): Boolean {
        when (status) {
            RollingBackhaulReviewStatus.NEW -> {
                when (next) {
                    RollingBackhaulReviewStatus.DONE, RollingBackhaulReviewStatus.FAILED -> return true
                }
                return false
            }
            RollingBackhaulReviewStatus.QUEUED -> return next == RollingBackhaulReviewStatus.DONE
            RollingBackhaulReviewStatus.RUNNING -> {
                when (next) {
                    RollingBackhaulReviewStatus.DONE -> return true
                }
                return false
            }
            RollingBackhaulReviewStatus.DONE -> {
                when (next) {
                    RollingBackhaulReviewStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingBackhaulReviewStatus) {
        if (!canMoveTo(next)) {
            error("rolling backhaul review $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"rolling backhaul review"} $code"

    fun weightedTotal(): Double = round((createdAt.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): RollingBackhaulReview {
        return RollingBackhaulReview(
            code = code,
            name = name,
            createdAt = createdAt,
            category = category,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rolling backhaul review in status new. */
fun newRollingBackhaulReview(code: String, name: String): RollingBackhaulReview {
    return RollingBackhaulReview(
        code = code,
        name = name,
        createdAt = 0,
        category = "",
        version = 0,
        status = RollingBackhaulReviewStatus.NEW,
    )
}
