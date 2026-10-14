package com.acme.transport.safety.coaching.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching batch is in its life cycle. */
enum class CoachingBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A coaching batch as the coaching team keeps it. */
data class CoachingBatch(
    var code: String,
    var name: String,
    var retries: Int,
    var odometer: Int,
    var attributes: MutableMap<String, String>,
    var status: CoachingBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingBatchStatus): Boolean {
        when (status) {
            CoachingBatchStatus.OPEN -> return next == CoachingBatchStatus.IN_REVIEW
            CoachingBatchStatus.IN_REVIEW -> return next == CoachingBatchStatus.REOPENED
            CoachingBatchStatus.RESOLVED -> {
                when (next) {
                    CoachingBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            CoachingBatchStatus.REOPENED -> {
                when (next) {
                    CoachingBatchStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingBatchStatus) {
        if (!canMoveTo(next)) {
            error("coaching batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveVolume(): Double = round(retries.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): CoachingBatch {
        return CoachingBatch(
            code = code,
            name = name,
            retries = retries,
            odometer = odometer,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a coaching batch in status open. */
fun newCoachingBatch(code: String, name: String): CoachingBatch {
    return CoachingBatch(
        code = code,
        name = name,
        retries = 0,
        odometer = 0,
        attributes = mutableMapOf<String, String>(),
        status = CoachingBatchStatus.OPEN,
    )
}
