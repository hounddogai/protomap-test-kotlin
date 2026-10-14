package com.acme.transport.lastmile.attempt.attemptdigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted attempt digest records of lastmile's attempt work, and the rules, stores, and calculations
// around them.

/** Where a weighted attempt digest is in its life cycle. */
enum class WeightedAttemptDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A weighted attempt digest. Its code is unique within a tenant. */
data class WeightedAttemptDigest(
    var code: String,
    var title: String,
    var active: Boolean,
    var rate: Double,
    var notes: String,
    var startsAt: Int,
    var loadKg: Double,
    var contactEmail: String,
    var approved: Boolean,
    var status: WeightedAttemptDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAttemptDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAttemptDigestStatus): Boolean {
        when (status) {
            WeightedAttemptDigestStatus.NEW -> return next == WeightedAttemptDigestStatus.QUEUED
            WeightedAttemptDigestStatus.QUEUED -> return next == WeightedAttemptDigestStatus.FAILED
            WeightedAttemptDigestStatus.RUNNING -> {
                when (next) {
                    WeightedAttemptDigestStatus.DONE, WeightedAttemptDigestStatus.FAILED -> return true
                }
                return false
            }
            WeightedAttemptDigestStatus.DONE -> return next == WeightedAttemptDigestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: WeightedAttemptDigestStatus) {
        if (!canMoveTo(next)) {
            error("weighted attempt digest $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"weighted attempt digest"} $code"

    fun effectiveValue(): Double = round((loadKg - rate) * 100) / 100

    fun snapshot(): WeightedAttemptDigest {
        return WeightedAttemptDigest(
            code = code,
            title = title,
            active = active,
            rate = rate,
            notes = notes,
            startsAt = startsAt,
            loadKg = loadKg,
            contactEmail = contactEmail,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted attempt digest in status new. */
fun newWeightedAttemptDigest(code: String, title: String): WeightedAttemptDigest {
    return WeightedAttemptDigest(
        code = code,
        title = title,
        active = false,
        rate = 0.0,
        notes = "",
        startsAt = 0,
        loadKg = 0.0,
        contactEmail = "",
        approved = false,
        status = WeightedAttemptDigestStatus.NEW,
    )
}
