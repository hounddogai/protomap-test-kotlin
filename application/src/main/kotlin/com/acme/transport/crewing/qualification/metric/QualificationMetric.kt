package com.acme.transport.crewing.qualification.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The qualification metric records of crewing's qualification work, and the rules, stores, and calculations
// around them.

/** Where a qualification metric is in its life cycle. */
enum class QualificationMetricStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A qualification metric as the qualification team keeps it. */
data class QualificationMetric(
    var code: String,
    var name: String,
    var revision: Int,
    var carrier: String,
    var vehicleId: String,
    var segment: String,
    var windowDays: Int,
    var attributes: MutableMap<String, String>,
    var description: String,
    var checksum: String,
    var notes: String,
    var status: QualificationMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationMetricStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationMetricStatus): Boolean {
        when (status) {
            QualificationMetricStatus.DRAFT -> return next == QualificationMetricStatus.PENDING || next == QualificationMetricStatus.SUSPENDED
            QualificationMetricStatus.PENDING -> return next == QualificationMetricStatus.CLOSED
            QualificationMetricStatus.ACTIVE -> {
                when (next) {
                    QualificationMetricStatus.SUSPENDED, QualificationMetricStatus.CLOSED -> return true
                }
                return false
            }
            QualificationMetricStatus.SUSPENDED -> {
                when (next) {
                    QualificationMetricStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: QualificationMetricStatus) {
        if (!canMoveTo(next)) {
            error("qualification metric $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"qualification metric"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round((windowDays.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): QualificationMetric {
        return QualificationMetric(
            code = code,
            name = name,
            revision = revision,
            carrier = carrier,
            vehicleId = vehicleId,
            segment = segment,
            windowDays = windowDays,
            attributes = attributes,
            description = description,
            checksum = checksum,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a qualification metric in status draft. */
fun newQualificationMetric(code: String, name: String): QualificationMetric {
    return QualificationMetric(
        code = code,
        name = name,
        revision = 0,
        carrier = "",
        vehicleId = "",
        segment = "",
        windowDays = 0,
        attributes = mutableMapOf<String, String>(),
        description = "",
        checksum = "",
        notes = "",
        status = QualificationMetricStatus.DRAFT,
    )
}
