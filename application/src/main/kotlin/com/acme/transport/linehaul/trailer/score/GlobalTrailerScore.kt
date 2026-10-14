package com.acme.transport.linehaul.trailer.score

import com.acme.transport.kit.text.normalize

// The global trailer score records of linehaul's trailer work, and the rules, stores, and calculations around
// them.

/** Where a global trailer score is in its life cycle. */
enum class GlobalTrailerScoreStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A global trailer score as the trailer team keeps it. */
data class GlobalTrailerScore(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var checksum: String,
    var active: Boolean,
    var threshold: Double,
    var status: GlobalTrailerScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTrailerScoreStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTrailerScoreStatus): Boolean {
        when (status) {
            GlobalTrailerScoreStatus.IDLE -> return next == GlobalTrailerScoreStatus.DRAINING
            GlobalTrailerScoreStatus.WARMING -> return next == GlobalTrailerScoreStatus.READY
            GlobalTrailerScoreStatus.READY -> return next == GlobalTrailerScoreStatus.STOPPED
            GlobalTrailerScoreStatus.DRAINING -> return next == GlobalTrailerScoreStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: GlobalTrailerScoreStatus) {
        if (!canMoveTo(next)) {
            error("global trailer score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = threshold

    fun snapshot(): GlobalTrailerScore {
        return GlobalTrailerScore(
            code = code,
            name = name,
            tags = tags,
            checksum = checksum,
            active = active,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global trailer score in status idle. */
fun newGlobalTrailerScore(code: String, name: String): GlobalTrailerScore {
    return GlobalTrailerScore(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        checksum = "",
        active = false,
        threshold = 0.0,
        status = GlobalTrailerScoreStatus.IDLE,
    )
}
