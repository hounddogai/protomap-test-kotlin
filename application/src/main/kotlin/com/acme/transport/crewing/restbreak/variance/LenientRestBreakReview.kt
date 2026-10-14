package com.acme.transport.crewing.restbreak.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient rest break review is in its life cycle. */
enum class LenientRestBreakReviewStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lenient rest break review as the rest break team keeps it. */
data class LenientRestBreakReview(
    var code: String,
    var name: String,
    var ratio: Double,
    var discount: Double,
    var phone: String,
    var status: LenientRestBreakReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRestBreakReviewStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRestBreakReviewStatus): Boolean {
        when (status) {
            LenientRestBreakReviewStatus.NEW -> {
                when (next) {
                    LenientRestBreakReviewStatus.QUEUED, LenientRestBreakReviewStatus.DONE -> return true
                }
                return false
            }
            LenientRestBreakReviewStatus.QUEUED -> {
                when (next) {
                    LenientRestBreakReviewStatus.DONE, LenientRestBreakReviewStatus.FAILED -> return true
                }
                return false
            }
            LenientRestBreakReviewStatus.RUNNING -> return next == LenientRestBreakReviewStatus.FAILED
            LenientRestBreakReviewStatus.DONE -> return next == LenientRestBreakReviewStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LenientRestBreakReviewStatus) {
        if (!canMoveTo(next)) {
            error("lenient rest break review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient rest break review"} $code"

    fun weightedTotal(): Double = round(ratio * discount * 100) / 100

    fun snapshot(): LenientRestBreakReview {
        return LenientRestBreakReview(
            code = code,
            name = name,
            ratio = ratio,
            discount = discount,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient rest break review in status new. */
fun newLenientRestBreakReview(code: String, name: String): LenientRestBreakReview {
    return LenientRestBreakReview(
        code = code,
        name = name,
        ratio = 0.0,
        discount = 0.0,
        phone = "",
        status = LenientRestBreakReviewStatus.NEW,
    )
}
