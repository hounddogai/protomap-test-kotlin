package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification draft is in its life cycle. */
enum class QualificationDraftStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A qualification draft as the qualification team keeps it. */
data class QualificationDraft(
    var code: String,
    var label: String,
    var archived: Boolean,
    var revision: Int,
    var etaMinutes: Int,
    var description: String,
    var distanceKm: Double,
    var status: QualificationDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationDraftStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationDraftStatus): Boolean {
        when (status) {
            QualificationDraftStatus.PROPOSED -> return next == QualificationDraftStatus.APPROVED
            QualificationDraftStatus.APPROVED -> return next == QualificationDraftStatus.REJECTED || next == QualificationDraftStatus.WITHDRAWN
            QualificationDraftStatus.REJECTED -> {
                when (next) {
                    QualificationDraftStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: QualificationDraftStatus) {
        if (!canMoveTo(next)) {
            error("qualification draft $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedExposure(): Double = round((revision.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): QualificationDraft {
        return QualificationDraft(
            code = code,
            label = label,
            archived = archived,
            revision = revision,
            etaMinutes = etaMinutes,
            description = description,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a qualification draft in status proposed. */
fun newQualificationDraft(code: String, label: String): QualificationDraft {
    return QualificationDraft(
        code = code,
        label = label,
        archived = false,
        revision = 0,
        etaMinutes = 0,
        description = "",
        distanceKm = 0.0,
        status = QualificationDraftStatus.PROPOSED,
    )
}
