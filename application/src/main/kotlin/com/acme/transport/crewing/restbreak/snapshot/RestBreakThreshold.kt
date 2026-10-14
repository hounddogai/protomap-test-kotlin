package com.acme.transport.crewing.restbreak.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break threshold is in its life cycle. */
enum class RestBreakThresholdStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A rest break threshold as the rest break team keeps it. */
data class RestBreakThreshold(
    var code: String,
    var name: String,
    var ownerId: String,
    var score: Double,
    var threshold: Double,
    var tags: MutableList<String>,
    var status: RestBreakThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakThresholdStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakThresholdStatus): Boolean {
        when (status) {
            RestBreakThresholdStatus.OPEN -> {
                when (next) {
                    RestBreakThresholdStatus.IN_REVIEW -> return true
                }
                return false
            }
            RestBreakThresholdStatus.IN_REVIEW -> {
                when (next) {
                    RestBreakThresholdStatus.RESOLVED -> return true
                }
                return false
            }
            RestBreakThresholdStatus.RESOLVED -> return next == RestBreakThresholdStatus.ARCHIVED
            RestBreakThresholdStatus.REOPENED -> {
                when (next) {
                    RestBreakThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakThresholdStatus) {
        if (!canMoveTo(next)) {
            error("rest break threshold $code cannot move from $status to $next")
        }
        status = next
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

    fun adjustedVolume(): Double = round(score * threshold * 100) / 100

    fun snapshot(): RestBreakThreshold {
        return RestBreakThreshold(
            code = code,
            name = name,
            ownerId = ownerId,
            score = score,
            threshold = threshold,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rest break threshold in status open. */
fun newRestBreakThreshold(code: String, name: String): RestBreakThreshold {
    return RestBreakThreshold(
        code = code,
        name = name,
        ownerId = "",
        score = 0.0,
        threshold = 0.0,
        tags = mutableListOf<String>(),
        status = RestBreakThresholdStatus.OPEN,
    )
}
