package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification mapping is in its life cycle. */
enum class QualificationMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A qualification mapping. Its code is unique within a tenant. */
data class QualificationMapping(
    var code: String,
    var name: String,
    var description: String,
    var margin: Double,
    var archived: Boolean,
    var attempts: Int,
    var sequence: Int,
    var endsAt: Int,
    var status: QualificationMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationMappingStatus): Boolean {
        when (status) {
            QualificationMappingStatus.OPEN -> {
                when (next) {
                    QualificationMappingStatus.IN_REVIEW -> return true
                }
                return false
            }
            QualificationMappingStatus.IN_REVIEW -> return next == QualificationMappingStatus.RESOLVED || next == QualificationMappingStatus.REOPENED
            QualificationMappingStatus.RESOLVED -> return next == QualificationMappingStatus.ARCHIVED
            QualificationMappingStatus.REOPENED -> return next == QualificationMappingStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: QualificationMappingStatus) {
        if (!canMoveTo(next)) {
            error("qualification mapping $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"qualification mapping"} $code"

    fun weightedLoad(): Double = round((margin + sequence.toDouble()) * 100) / 100

    fun snapshot(): QualificationMapping {
        return QualificationMapping(
            code = code,
            name = name,
            description = description,
            margin = margin,
            archived = archived,
            attempts = attempts,
            sequence = sequence,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a qualification mapping in status open. */
fun newQualificationMapping(code: String, name: String): QualificationMapping {
    return QualificationMapping(
        code = code,
        name = name,
        description = "",
        margin = 0.0,
        archived = false,
        attempts = 0,
        sequence = 0,
        endsAt = 0,
        status = QualificationMappingStatus.OPEN,
    )
}
