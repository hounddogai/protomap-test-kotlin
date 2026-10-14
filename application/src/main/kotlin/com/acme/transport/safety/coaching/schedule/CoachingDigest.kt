package com.acme.transport.safety.coaching.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching digest is in its life cycle. */
enum class CoachingDigestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a coaching digest and its current status. */
data class CoachingDigest(
    var code: String,
    var title: String,
    var dock: String,
    var margin: Double,
    var score: Double,
    var checksum: String,
    var contactEmail: String,
    var tags: MutableList<String>,
    var status: CoachingDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingDigestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingDigestStatus): Boolean {
        when (status) {
            CoachingDigestStatus.PROPOSED -> return next == CoachingDigestStatus.APPROVED
            CoachingDigestStatus.APPROVED -> return next == CoachingDigestStatus.REJECTED
            CoachingDigestStatus.REJECTED -> {
                when (next) {
                    CoachingDigestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingDigestStatus) {
        if (!canMoveTo(next)) {
            error("coaching digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netExposure(): Double = round(score * margin * 100) / 100

    fun snapshot(): CoachingDigest {
        return CoachingDigest(
            code = code,
            title = title,
            dock = dock,
            margin = margin,
            score = score,
            checksum = checksum,
            contactEmail = contactEmail,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a coaching digest in status proposed. */
fun newCoachingDigest(code: String, title: String): CoachingDigest {
    return CoachingDigest(
        code = code,
        title = title,
        dock = "",
        margin = 0.0,
        score = 0.0,
        checksum = "",
        contactEmail = "",
        tags = mutableListOf<String>(),
        status = CoachingDigestStatus.PROPOSED,
    )
}
