package com.acme.transport.linehaul.tender.fallbacktenderbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled tender batch records of linehaul's tender work, and the rules, stores, and calculations
// around them.

/** Where a scheduled tender batch is in its life cycle. */
enum class ScheduledTenderBatchStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A scheduled tender batch as the tender team keeps it. */
data class ScheduledTenderBatch(
    var code: String,
    var title: String,
    var description: String,
    var updatedAt: Int,
    var createdAt: Int,
    var visible: Boolean,
    var summary: String,
    var status: ScheduledTenderBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTenderBatchStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTenderBatchStatus): Boolean {
        when (status) {
            ScheduledTenderBatchStatus.CREATED -> {
                when (next) {
                    ScheduledTenderBatchStatus.VALIDATED, ScheduledTenderBatchStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledTenderBatchStatus.VALIDATED -> return next == ScheduledTenderBatchStatus.PUBLISHED || next == ScheduledTenderBatchStatus.RETIRED
            ScheduledTenderBatchStatus.PUBLISHED -> return next == ScheduledTenderBatchStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledTenderBatchStatus) {
        if (!canMoveTo(next)) {
            error("scheduled tender batch $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun netTotal(): Double = round((createdAt.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledTenderBatch {
        return ScheduledTenderBatch(
            code = code,
            title = title,
            description = description,
            updatedAt = updatedAt,
            createdAt = createdAt,
            visible = visible,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled tender batch in status created. */
fun newScheduledTenderBatch(code: String, title: String): ScheduledTenderBatch {
    return ScheduledTenderBatch(
        code = code,
        title = title,
        description = "",
        updatedAt = 0,
        createdAt = 0,
        visible = false,
        summary = "",
        status = ScheduledTenderBatchStatus.CREATED,
    )
}
