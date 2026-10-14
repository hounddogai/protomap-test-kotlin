package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.kit.text.normalize

// The near miss variance records of safety's near miss work, and the rules, stores, and calculations around
// them.

/** Where a near miss variance is in its life cycle. */
enum class NearMissVarianceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A near miss variance. Its code is unique within a tenant. */
data class NearMissVariance(
    var code: String,
    var title: String,
    var segment: String,
    var channel: String,
    var reviewer: String,
    var trailerId: String,
    var score: Double,
    var status: NearMissVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissVarianceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissVarianceStatus): Boolean {
        when (status) {
            NearMissVarianceStatus.CREATED -> {
                when (next) {
                    NearMissVarianceStatus.PUBLISHED -> return true
                }
                return false
            }
            NearMissVarianceStatus.VALIDATED -> {
                when (next) {
                    NearMissVarianceStatus.PUBLISHED, NearMissVarianceStatus.RETIRED -> return true
                }
                return false
            }
            NearMissVarianceStatus.PUBLISHED -> return next == NearMissVarianceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: NearMissVarianceStatus) {
        if (!canMoveTo(next)) {
            error("near miss variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = score

    fun snapshot(): NearMissVariance {
        return NearMissVariance(
            code = code,
            title = title,
            segment = segment,
            channel = channel,
            reviewer = reviewer,
            trailerId = trailerId,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a near miss variance in status created. */
fun newNearMissVariance(code: String, title: String): NearMissVariance {
    return NearMissVariance(
        code = code,
        title = title,
        segment = "",
        channel = "",
        reviewer = "",
        trailerId = "",
        score = 0.0,
        status = NearMissVarianceStatus.CREATED,
    )
}
