package com.acme.transport.customs.inspection.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection batch records of customs's inspection work, and the rules, stores, and calculations around
// them.

/** Where a inspection batch is in its life cycle. */
enum class InspectionBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A inspection batch. Its code is unique within a tenant. */
data class InspectionBatch(
    var code: String,
    var label: String,
    var endsAt: Int,
    var margin: Double,
    var weight: Double,
    var updatedAt: Int,
    var status: InspectionBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionBatchStatus): Boolean {
        when (status) {
            InspectionBatchStatus.DRAFT -> return next == InspectionBatchStatus.PENDING || next == InspectionBatchStatus.CLOSED
            InspectionBatchStatus.PENDING -> {
                when (next) {
                    InspectionBatchStatus.CLOSED -> return true
                }
                return false
            }
            InspectionBatchStatus.ACTIVE -> return next == InspectionBatchStatus.SUSPENDED || next == InspectionBatchStatus.CLOSED
            InspectionBatchStatus.SUSPENDED -> return next == InspectionBatchStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: InspectionBatchStatus) {
        if (!canMoveTo(next)) {
            error("inspection batch $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedExposure(): Double = round(endsAt.toDouble() * margin * 100) / 100

    fun snapshot(): InspectionBatch {
        return InspectionBatch(
            code = code,
            label = label,
            endsAt = endsAt,
            margin = margin,
            weight = weight,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a inspection batch in status draft. */
fun newInspectionBatch(code: String, label: String): InspectionBatch {
    return InspectionBatch(
        code = code,
        label = label,
        endsAt = 0,
        margin = 0.0,
        weight = 0.0,
        updatedAt = 0,
        status = InspectionBatchStatus.DRAFT,
    )
}
