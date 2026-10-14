package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification entry is in its life cycle. */
enum class QualificationEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A qualification entry. Its code is unique within a tenant. */
data class QualificationEntry(
    var code: String,
    var label: String,
    var revision: Int,
    var cost: Double,
    var category: String,
    var trailerId: String,
    var expiresAt: Int,
    var name: String,
    var status: QualificationEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationEntryStatus): Boolean {
        when (status) {
            QualificationEntryStatus.OPEN -> return next == QualificationEntryStatus.RESOLVED || next == QualificationEntryStatus.ARCHIVED
            QualificationEntryStatus.IN_REVIEW -> return next == QualificationEntryStatus.REOPENED
            QualificationEntryStatus.RESOLVED -> return next == QualificationEntryStatus.ARCHIVED
            QualificationEntryStatus.REOPENED -> {
                when (next) {
                    QualificationEntryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: QualificationEntryStatus) {
        if (!canMoveTo(next)) {
            error("qualification entry $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedValue(): Double = round((revision.toDouble() + cost) * 100) / 100

    fun snapshot(): QualificationEntry {
        return QualificationEntry(
            code = code,
            label = label,
            revision = revision,
            cost = cost,
            category = category,
            trailerId = trailerId,
            expiresAt = expiresAt,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a qualification entry in status open. */
fun newQualificationEntry(code: String, label: String): QualificationEntry {
    return QualificationEntry(
        code = code,
        label = label,
        revision = 0,
        cost = 0.0,
        category = "",
        trailerId = "",
        expiresAt = 0,
        name = "",
        status = QualificationEntryStatus.OPEN,
    )
}
