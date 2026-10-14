package com.acme.transport.crewing.qualification.metric

import com.acme.transport.kit.text.normalize

/** Where a global qualification checkpoint is in its life cycle. */
enum class GlobalQualificationCheckpointStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A global qualification checkpoint as the qualification team keeps it. */
data class GlobalQualificationCheckpoint(
    var code: String,
    var name: String,
    var source: String,
    var summary: String,
    var channel: String,
    var visible: Boolean,
    var quantity: Int,
    var status: GlobalQualificationCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalQualificationCheckpointStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalQualificationCheckpointStatus): Boolean {
        when (status) {
            GlobalQualificationCheckpointStatus.OPEN -> return next == GlobalQualificationCheckpointStatus.IN_REVIEW || next == GlobalQualificationCheckpointStatus.REOPENED
            GlobalQualificationCheckpointStatus.IN_REVIEW -> {
                when (next) {
                    GlobalQualificationCheckpointStatus.RESOLVED, GlobalQualificationCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalQualificationCheckpointStatus.RESOLVED -> return next == GlobalQualificationCheckpointStatus.REOPENED
            GlobalQualificationCheckpointStatus.REOPENED -> return next == GlobalQualificationCheckpointStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: GlobalQualificationCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("global qualification checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global qualification checkpoint"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): GlobalQualificationCheckpoint {
        return GlobalQualificationCheckpoint(
            code = code,
            name = name,
            source = source,
            summary = summary,
            channel = channel,
            visible = visible,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a global qualification checkpoint in status open. */
fun newGlobalQualificationCheckpoint(code: String, name: String): GlobalQualificationCheckpoint {
    return GlobalQualificationCheckpoint(
        code = code,
        name = name,
        source = "",
        summary = "",
        channel = "",
        visible = false,
        quantity = 0,
        status = GlobalQualificationCheckpointStatus.OPEN,
    )
}
