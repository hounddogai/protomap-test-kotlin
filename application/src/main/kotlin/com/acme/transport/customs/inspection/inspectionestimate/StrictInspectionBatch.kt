package com.acme.transport.customs.inspection.inspectionestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict inspection batch is in its life cycle. */
enum class StrictInspectionBatchStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One strict inspection batch, with what the planners need to act on it. */
data class StrictInspectionBatch(
    var code: String,
    var label: String,
    var ratio: Double,
    var contactEmail: String,
    var name: String,
    var pallets: Int,
    var retries: Int,
    var reviewer: String,
    var budget: Double,
    var status: StrictInspectionBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictInspectionBatchStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictInspectionBatchStatus): Boolean {
        when (status) {
            StrictInspectionBatchStatus.NEW -> {
                when (next) {
                    StrictInspectionBatchStatus.FAILED -> return true
                }
                return false
            }
            StrictInspectionBatchStatus.QUEUED -> return next == StrictInspectionBatchStatus.RUNNING
            StrictInspectionBatchStatus.RUNNING -> return next == StrictInspectionBatchStatus.DONE
            StrictInspectionBatchStatus.DONE -> return next == StrictInspectionBatchStatus.FAILED
        }
        return false
    }

    fun moveTo(next: StrictInspectionBatchStatus) {
        if (!canMoveTo(next)) {
            error("strict inspection batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedTotal(): Double = round((ratio + pallets.toDouble()) * 100) / 100

    fun snapshot(): StrictInspectionBatch {
        return StrictInspectionBatch(
            code = code,
            label = label,
            ratio = ratio,
            contactEmail = contactEmail,
            name = name,
            pallets = pallets,
            retries = retries,
            reviewer = reviewer,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict inspection batch in status new. */
fun newStrictInspectionBatch(code: String, label: String): StrictInspectionBatch {
    return StrictInspectionBatch(
        code = code,
        label = label,
        ratio = 0.0,
        contactEmail = "",
        name = "",
        pallets = 0,
        retries = 0,
        reviewer = "",
        budget = 0.0,
        status = StrictInspectionBatchStatus.NEW,
    )
}
