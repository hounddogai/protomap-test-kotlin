package com.acme.transport.safety.coaching.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching variance is in its life cycle. */
enum class CoachingVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One coaching variance, with what the planners need to act on it. */
data class CoachingVariance(
    var code: String,
    var name: String,
    var batchSize: Int,
    var region: String,
    var retries: Int,
    var visible: Boolean,
    var pinned: Boolean,
    var sequence: Int,
    var status: CoachingVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingVarianceStatus): Boolean {
        when (status) {
            CoachingVarianceStatus.NEW -> return next == CoachingVarianceStatus.DONE
            CoachingVarianceStatus.QUEUED -> {
                when (next) {
                    CoachingVarianceStatus.RUNNING, CoachingVarianceStatus.FAILED -> return true
                }
                return false
            }
            CoachingVarianceStatus.RUNNING -> {
                when (next) {
                    CoachingVarianceStatus.DONE, CoachingVarianceStatus.FAILED -> return true
                }
                return false
            }
            CoachingVarianceStatus.DONE -> return next == CoachingVarianceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CoachingVarianceStatus) {
        if (!canMoveTo(next)) {
            error("coaching variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"coaching variance"} $code"

    fun weightedLoad(): Double = round((retries.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): CoachingVariance {
        return CoachingVariance(
            code = code,
            name = name,
            batchSize = batchSize,
            region = region,
            retries = retries,
            visible = visible,
            pinned = pinned,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a coaching variance in status new. */
fun newCoachingVariance(code: String, name: String): CoachingVariance {
    return CoachingVariance(
        code = code,
        name = name,
        batchSize = 0,
        region = "",
        retries = 0,
        visible = false,
        pinned = false,
        sequence = 0,
        status = CoachingVarianceStatus.NEW,
    )
}
