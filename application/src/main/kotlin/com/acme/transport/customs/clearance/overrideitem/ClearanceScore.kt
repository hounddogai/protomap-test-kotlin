package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance score is in its life cycle. */
enum class ClearanceScoreStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One clearance score, with what the planners need to act on it. */
data class ClearanceScore(
    var code: String,
    var label: String,
    var score: Double,
    var endsAt: Int,
    var segment: String,
    var retries: Int,
    var channel: String,
    var ratio: Double,
    var dock: String,
    var maximum: Double,
    var createdAt: Int,
    var status: ClearanceScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceScoreStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceScoreStatus): Boolean {
        when (status) {
            ClearanceScoreStatus.PROPOSED -> {
                when (next) {
                    ClearanceScoreStatus.APPROVED -> return true
                }
                return false
            }
            ClearanceScoreStatus.APPROVED -> return next == ClearanceScoreStatus.REJECTED || next == ClearanceScoreStatus.WITHDRAWN
            ClearanceScoreStatus.REJECTED -> return next == ClearanceScoreStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ClearanceScoreStatus) {
        if (!canMoveTo(next)) {
            error("clearance score $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedExposure(): Double = round(ratio * maximum * 100) / 100

    fun snapshot(): ClearanceScore {
        return ClearanceScore(
            code = code,
            label = label,
            score = score,
            endsAt = endsAt,
            segment = segment,
            retries = retries,
            channel = channel,
            ratio = ratio,
            dock = dock,
            maximum = maximum,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a clearance score in status proposed. */
fun newClearanceScore(code: String, label: String): ClearanceScore {
    return ClearanceScore(
        code = code,
        label = label,
        score = 0.0,
        endsAt = 0,
        segment = "",
        retries = 0,
        channel = "",
        ratio = 0.0,
        dock = "",
        maximum = 0.0,
        createdAt = 0,
        status = ClearanceScoreStatus.PROPOSED,
    )
}
