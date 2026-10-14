package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive broker assignment is in its life cycle. */
enum class AdaptiveBrokerAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a adaptive broker assignment and its current status. */
data class AdaptiveBrokerAssignment(
    var code: String,
    var title: String,
    var endsAt: Int,
    var etaMinutes: Int,
    var budget: Double,
    var stopCount: Int,
    var batchSize: Int,
    var attributes: MutableMap<String, String>,
    var status: AdaptiveBrokerAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveBrokerAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveBrokerAssignmentStatus): Boolean {
        when (status) {
            AdaptiveBrokerAssignmentStatus.IDLE -> return next == AdaptiveBrokerAssignmentStatus.DRAINING || next == AdaptiveBrokerAssignmentStatus.STOPPED
            AdaptiveBrokerAssignmentStatus.WARMING -> {
                when (next) {
                    AdaptiveBrokerAssignmentStatus.READY, AdaptiveBrokerAssignmentStatus.DRAINING -> return true
                }
                return false
            }
            AdaptiveBrokerAssignmentStatus.READY -> {
                when (next) {
                    AdaptiveBrokerAssignmentStatus.DRAINING, AdaptiveBrokerAssignmentStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveBrokerAssignmentStatus.DRAINING -> {
                when (next) {
                    AdaptiveBrokerAssignmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveBrokerAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("adaptive broker assignment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedValue(): Double = round((budget + endsAt.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveBrokerAssignment {
        return AdaptiveBrokerAssignment(
            code = code,
            title = title,
            endsAt = endsAt,
            etaMinutes = etaMinutes,
            budget = budget,
            stopCount = stopCount,
            batchSize = batchSize,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive broker assignment in status idle. */
fun newAdaptiveBrokerAssignment(code: String, title: String): AdaptiveBrokerAssignment {
    return AdaptiveBrokerAssignment(
        code = code,
        title = title,
        endsAt = 0,
        etaMinutes = 0,
        budget = 0.0,
        stopCount = 0,
        batchSize = 0,
        attributes = mutableMapOf<String, String>(),
        status = AdaptiveBrokerAssignmentStatus.IDLE,
    )
}
