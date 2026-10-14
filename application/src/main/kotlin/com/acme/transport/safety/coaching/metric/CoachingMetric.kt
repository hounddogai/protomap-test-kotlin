package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The coaching metric records of safety's coaching work, and the rules, stores, and calculations around them.

/** Where a coaching metric is in its life cycle. */
enum class CoachingMetricStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A coaching metric as the coaching team keeps it. */
data class CoachingMetric(
    var code: String,
    var label: String,
    var ratio: Double,
    var archived: Boolean,
    var updatedAt: Int,
    var attributes: MutableMap<String, String>,
    var capacity: Int,
    var status: CoachingMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingMetricStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingMetricStatus): Boolean {
        when (status) {
            CoachingMetricStatus.PLANNED -> {
                when (next) {
                    CoachingMetricStatus.COMPLETED, CoachingMetricStatus.CANCELLED -> return true
                }
                return false
            }
            CoachingMetricStatus.SCHEDULED -> return next == CoachingMetricStatus.COMPLETED
            CoachingMetricStatus.IN_PROGRESS -> {
                when (next) {
                    CoachingMetricStatus.COMPLETED -> return true
                }
                return false
            }
            CoachingMetricStatus.COMPLETED -> {
                when (next) {
                    CoachingMetricStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingMetricStatus) {
        if (!canMoveTo(next)) {
            error("coaching metric $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedTotal(): Double = round(ratio * capacity.toDouble() * 100) / 100

    fun snapshot(): CoachingMetric {
        return CoachingMetric(
            code = code,
            label = label,
            ratio = ratio,
            archived = archived,
            updatedAt = updatedAt,
            attributes = attributes,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a coaching metric in status planned. */
fun newCoachingMetric(code: String, label: String): CoachingMetric {
    return CoachingMetric(
        code = code,
        label = label,
        ratio = 0.0,
        archived = false,
        updatedAt = 0,
        attributes = mutableMapOf<String, String>(),
        capacity = 0,
        status = CoachingMetricStatus.PLANNED,
    )
}
