package com.acme.transport.crewing.qualification.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification batch is in its life cycle. */
enum class QualificationBatchStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A qualification batch. Its code is unique within a tenant. */
data class QualificationBatch(
    var code: String,
    var name: String,
    var carrier: String,
    var createdAt: Int,
    var ratio: Double,
    var status: QualificationBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationBatchStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationBatchStatus): Boolean {
        when (status) {
            QualificationBatchStatus.IDLE -> {
                when (next) {
                    QualificationBatchStatus.DRAINING -> return true
                }
                return false
            }
            QualificationBatchStatus.WARMING -> return next == QualificationBatchStatus.READY || next == QualificationBatchStatus.DRAINING
            QualificationBatchStatus.READY -> return next == QualificationBatchStatus.DRAINING || next == QualificationBatchStatus.STOPPED
            QualificationBatchStatus.DRAINING -> return next == QualificationBatchStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: QualificationBatchStatus) {
        if (!canMoveTo(next)) {
            error("qualification batch $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun effectiveValue(): Double = round(ratio * createdAt.toDouble() * 100) / 100

    fun snapshot(): QualificationBatch {
        return QualificationBatch(
            code = code,
            name = name,
            carrier = carrier,
            createdAt = createdAt,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a qualification batch in status idle. */
fun newQualificationBatch(code: String, name: String): QualificationBatch {
    return QualificationBatch(
        code = code,
        name = name,
        carrier = "",
        createdAt = 0,
        ratio = 0.0,
        status = QualificationBatchStatus.IDLE,
    )
}
