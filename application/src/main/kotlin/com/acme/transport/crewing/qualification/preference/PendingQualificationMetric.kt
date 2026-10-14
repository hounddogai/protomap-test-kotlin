package com.acme.transport.crewing.qualification.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending qualification metric is in its life cycle. */
enum class PendingQualificationMetricStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a pending qualification metric and its current status. */
data class PendingQualificationMetric(
    var code: String,
    var label: String,
    var archived: Boolean,
    var attributes: MutableMap<String, String>,
    var budget: Double,
    var title: String,
    var maximum: Double,
    var status: PendingQualificationMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingQualificationMetricStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingQualificationMetricStatus): Boolean {
        when (status) {
            PendingQualificationMetricStatus.OPEN -> return next == PendingQualificationMetricStatus.IN_REVIEW
            PendingQualificationMetricStatus.IN_REVIEW -> return next == PendingQualificationMetricStatus.REOPENED
            PendingQualificationMetricStatus.RESOLVED -> {
                when (next) {
                    PendingQualificationMetricStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingQualificationMetricStatus.REOPENED -> {
                when (next) {
                    PendingQualificationMetricStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingQualificationMetricStatus) {
        if (!canMoveTo(next)) {
            error("pending qualification metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending qualification metric"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round((maximum + budget) * 100) / 100

    fun snapshot(): PendingQualificationMetric {
        return PendingQualificationMetric(
            code = code,
            label = label,
            archived = archived,
            attributes = attributes,
            budget = budget,
            title = title,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending qualification metric in status open. */
fun newPendingQualificationMetric(code: String, label: String): PendingQualificationMetric {
    return PendingQualificationMetric(
        code = code,
        label = label,
        archived = false,
        attributes = mutableMapOf<String, String>(),
        budget = 0.0,
        title = "",
        maximum = 0.0,
        status = PendingQualificationMetricStatus.OPEN,
    )
}
