package com.acme.transport.crewing.qualification.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The qualification checkpoint records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a qualification checkpoint is in its life cycle. */
enum class QualificationCheckpointStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A qualification checkpoint as the qualification team keeps it. */
data class QualificationCheckpoint(
    var code: String,
    var title: String,
    var category: String,
    var quantity: Int,
    var batchSize: Int,
    var weight: Double,
    var sequence: Int,
    var startsAt: Int,
    var summary: String,
    var segment: String,
    var pallets: Int,
    var status: QualificationCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationCheckpointStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationCheckpointStatus): Boolean {
        when (status) {
            QualificationCheckpointStatus.REQUESTED -> {
                when (next) {
                    QualificationCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
            QualificationCheckpointStatus.GRANTED -> {
                when (next) {
                    QualificationCheckpointStatus.REVOKED, QualificationCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
            QualificationCheckpointStatus.REVOKED -> {
                when (next) {
                    QualificationCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: QualificationCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("qualification checkpoint $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedVolume(): Double = round(batchSize.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): QualificationCheckpoint {
        return QualificationCheckpoint(
            code = code,
            title = title,
            category = category,
            quantity = quantity,
            batchSize = batchSize,
            weight = weight,
            sequence = sequence,
            startsAt = startsAt,
            summary = summary,
            segment = segment,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a qualification checkpoint in status requested. */
fun newQualificationCheckpoint(code: String, title: String): QualificationCheckpoint {
    return QualificationCheckpoint(
        code = code,
        title = title,
        category = "",
        quantity = 0,
        batchSize = 0,
        weight = 0.0,
        sequence = 0,
        startsAt = 0,
        summary = "",
        segment = "",
        pallets = 0,
        status = QualificationCheckpointStatus.REQUESTED,
    )
}
