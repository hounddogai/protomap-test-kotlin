package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched appointment variance records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a batched appointment variance is in its life cycle. */
enum class BatchedAppointmentVarianceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A batched appointment variance. Its code is unique within a tenant. */
data class BatchedAppointmentVariance(
    var code: String,
    var label: String,
    var score: Double,
    var budget: Double,
    var category: String,
    var margin: Double,
    var version: Int,
    var archived: Boolean,
    var trailerId: String,
    var status: BatchedAppointmentVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAppointmentVarianceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAppointmentVarianceStatus): Boolean {
        when (status) {
            BatchedAppointmentVarianceStatus.CREATED -> return next == BatchedAppointmentVarianceStatus.PUBLISHED
            BatchedAppointmentVarianceStatus.VALIDATED -> {
                when (next) {
                    BatchedAppointmentVarianceStatus.PUBLISHED -> return true
                }
                return false
            }
            BatchedAppointmentVarianceStatus.PUBLISHED -> return next == BatchedAppointmentVarianceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BatchedAppointmentVarianceStatus) {
        if (!canMoveTo(next)) {
            error("batched appointment variance $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun netTotal(): Double = round((margin - version.toDouble()) * 100) / 100

    fun snapshot(): BatchedAppointmentVariance {
        return BatchedAppointmentVariance(
            code = code,
            label = label,
            score = score,
            budget = budget,
            category = category,
            margin = margin,
            version = version,
            archived = archived,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a batched appointment variance in status created. */
fun newBatchedAppointmentVariance(code: String, label: String): BatchedAppointmentVariance {
    return BatchedAppointmentVariance(
        code = code,
        label = label,
        score = 0.0,
        budget = 0.0,
        category = "",
        margin = 0.0,
        version = 0,
        archived = false,
        trailerId = "",
        status = BatchedAppointmentVarianceStatus.CREATED,
    )
}
