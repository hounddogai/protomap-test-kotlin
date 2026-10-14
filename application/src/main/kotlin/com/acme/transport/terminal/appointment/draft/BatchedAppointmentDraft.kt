package com.acme.transport.terminal.appointment.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched appointment draft records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a batched appointment draft is in its life cycle. */
enum class BatchedAppointmentDraftStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched appointment draft. Its code is unique within a tenant. */
data class BatchedAppointmentDraft(
    var code: String,
    var name: String,
    var threshold: Double,
    var summary: String,
    var reviewer: String,
    var updatedAt: Int,
    var shiftCode: String,
    var active: Boolean,
    var label: String,
    var status: BatchedAppointmentDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAppointmentDraftStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAppointmentDraftStatus): Boolean {
        when (status) {
            BatchedAppointmentDraftStatus.IDLE -> return next == BatchedAppointmentDraftStatus.WARMING
            BatchedAppointmentDraftStatus.WARMING -> {
                when (next) {
                    BatchedAppointmentDraftStatus.READY -> return true
                }
                return false
            }
            BatchedAppointmentDraftStatus.READY -> {
                when (next) {
                    BatchedAppointmentDraftStatus.DRAINING -> return true
                }
                return false
            }
            BatchedAppointmentDraftStatus.DRAINING -> {
                when (next) {
                    BatchedAppointmentDraftStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedAppointmentDraftStatus) {
        if (!canMoveTo(next)) {
            error("batched appointment draft $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($name)"

    fun netVolume(): Double = round((updatedAt.toDouble() + threshold) * 100) / 100

    fun snapshot(): BatchedAppointmentDraft {
        return BatchedAppointmentDraft(
            code = code,
            name = name,
            threshold = threshold,
            summary = summary,
            reviewer = reviewer,
            updatedAt = updatedAt,
            shiftCode = shiftCode,
            active = active,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched appointment draft in status idle. */
fun newBatchedAppointmentDraft(code: String, name: String): BatchedAppointmentDraft {
    return BatchedAppointmentDraft(
        code = code,
        name = name,
        threshold = 0.0,
        summary = "",
        reviewer = "",
        updatedAt = 0,
        shiftCode = "",
        active = false,
        label = "",
        status = BatchedAppointmentDraftStatus.IDLE,
    )
}
