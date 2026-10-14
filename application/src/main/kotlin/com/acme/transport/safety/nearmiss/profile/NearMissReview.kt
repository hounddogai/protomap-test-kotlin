package com.acme.transport.safety.nearmiss.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss review is in its life cycle. */
enum class NearMissReviewStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A near miss review. Its code is unique within a tenant. */
data class NearMissReview(
    var code: String,
    var name: String,
    var sequence: Int,
    var tags: MutableList<String>,
    var revision: Int,
    var title: String,
    var windowDays: Int,
    var active: Boolean,
    var shiftCode: String,
    var status: NearMissReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissReviewStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissReviewStatus): Boolean {
        when (status) {
            NearMissReviewStatus.OPEN -> {
                when (next) {
                    NearMissReviewStatus.RESOLVED -> return true
                }
                return false
            }
            NearMissReviewStatus.IN_REVIEW -> return next == NearMissReviewStatus.ARCHIVED
            NearMissReviewStatus.RESOLVED -> return next == NearMissReviewStatus.REOPENED || next == NearMissReviewStatus.ARCHIVED
            NearMissReviewStatus.REOPENED -> return next == NearMissReviewStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: NearMissReviewStatus) {
        if (!canMoveTo(next)) {
            error("near miss review $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"near miss review"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveVolume(): Double = round(windowDays.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): NearMissReview {
        return NearMissReview(
            code = code,
            name = name,
            sequence = sequence,
            tags = tags,
            revision = revision,
            title = title,
            windowDays = windowDays,
            active = active,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a near miss review in status open. */
fun newNearMissReview(code: String, name: String): NearMissReview {
    return NearMissReview(
        code = code,
        name = name,
        sequence = 0,
        tags = mutableListOf<String>(),
        revision = 0,
        title = "",
        windowDays = 0,
        active = false,
        shiftCode = "",
        status = NearMissReviewStatus.OPEN,
    )
}
