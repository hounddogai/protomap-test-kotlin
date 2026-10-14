package com.acme.transport.crewing.qualification.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification manifest is in its life cycle. */
enum class QualificationManifestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One qualification manifest, with what the planners need to act on it. */
data class QualificationManifest(
    var code: String,
    var name: String,
    var maximum: Double,
    var segment: String,
    var cost: Double,
    var capacity: Int,
    var status: QualificationManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationManifestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationManifestStatus): Boolean {
        when (status) {
            QualificationManifestStatus.PLANNED -> return next == QualificationManifestStatus.SCHEDULED || next == QualificationManifestStatus.IN_PROGRESS
            QualificationManifestStatus.SCHEDULED -> return next == QualificationManifestStatus.COMPLETED || next == QualificationManifestStatus.CANCELLED
            QualificationManifestStatus.IN_PROGRESS -> return next == QualificationManifestStatus.COMPLETED || next == QualificationManifestStatus.CANCELLED
            QualificationManifestStatus.COMPLETED -> {
                when (next) {
                    QualificationManifestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: QualificationManifestStatus) {
        if (!canMoveTo(next)) {
            error("qualification manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedTotal(): Double = round(cost * maximum * 100) / 100

    fun snapshot(): QualificationManifest {
        return QualificationManifest(
            code = code,
            name = name,
            maximum = maximum,
            segment = segment,
            cost = cost,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a qualification manifest in status planned. */
fun newQualificationManifest(code: String, name: String): QualificationManifest {
    return QualificationManifest(
        code = code,
        name = name,
        maximum = 0.0,
        segment = "",
        cost = 0.0,
        capacity = 0,
        status = QualificationManifestStatus.PLANNED,
    )
}
