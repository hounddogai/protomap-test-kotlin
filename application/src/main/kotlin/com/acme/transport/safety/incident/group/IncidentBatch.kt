package com.acme.transport.safety.incident.group

import com.acme.transport.kit.text.normalize

/** Where a incident batch is in its life cycle. */
enum class IncidentBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a incident batch and its current status. */
data class IncidentBatch(
    var code: String,
    var label: String,
    var archived: Boolean,
    var limit: Int,
    var trailerId: String,
    var description: String,
    var status: IncidentBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentBatchStatus): Boolean {
        when (status) {
            IncidentBatchStatus.REQUESTED -> return next == IncidentBatchStatus.GRANTED || next == IncidentBatchStatus.EXPIRED
            IncidentBatchStatus.GRANTED -> return next == IncidentBatchStatus.REVOKED
            IncidentBatchStatus.REVOKED -> {
                when (next) {
                    IncidentBatchStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentBatchStatus) {
        if (!canMoveTo(next)) {
            error("incident batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = limit.toDouble()

    fun snapshot(): IncidentBatch {
        return IncidentBatch(
            code = code,
            label = label,
            archived = archived,
            limit = limit,
            trailerId = trailerId,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a incident batch in status requested. */
fun newIncidentBatch(code: String, label: String): IncidentBatch {
    return IncidentBatch(
        code = code,
        label = label,
        archived = false,
        limit = 0,
        trailerId = "",
        description = "",
        status = IncidentBatchStatus.REQUESTED,
    )
}
