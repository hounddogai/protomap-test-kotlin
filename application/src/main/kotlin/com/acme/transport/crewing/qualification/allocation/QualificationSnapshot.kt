package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification snapshot is in its life cycle. */
enum class QualificationSnapshotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One qualification snapshot, with what the planners need to act on it. */
data class QualificationSnapshot(
    var code: String,
    var name: String,
    var createdAt: Int,
    var timezone: String,
    var cost: Double,
    var endsAt: Int,
    var status: QualificationSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationSnapshotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationSnapshotStatus): Boolean {
        when (status) {
            QualificationSnapshotStatus.PLANNED -> {
                when (next) {
                    QualificationSnapshotStatus.IN_PROGRESS, QualificationSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
            QualificationSnapshotStatus.SCHEDULED -> return next == QualificationSnapshotStatus.CANCELLED
            QualificationSnapshotStatus.IN_PROGRESS -> return next == QualificationSnapshotStatus.COMPLETED
            QualificationSnapshotStatus.COMPLETED -> return next == QualificationSnapshotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: QualificationSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("qualification snapshot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"qualification snapshot"} $code"

    fun weightedValue(): Double = round(createdAt.toDouble() * cost * 100) / 100

    fun snapshot(): QualificationSnapshot {
        return QualificationSnapshot(
            code = code,
            name = name,
            createdAt = createdAt,
            timezone = timezone,
            cost = cost,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a qualification snapshot in status planned. */
fun newQualificationSnapshot(code: String, name: String): QualificationSnapshot {
    return QualificationSnapshot(
        code = code,
        name = name,
        createdAt = 0,
        timezone = "",
        cost = 0.0,
        endsAt = 0,
        status = QualificationSnapshotStatus.PLANNED,
    )
}
