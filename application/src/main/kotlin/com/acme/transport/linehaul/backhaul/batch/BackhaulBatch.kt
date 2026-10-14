package com.acme.transport.linehaul.backhaul.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The backhaul batch records of linehaul's backhaul work, and the rules, stores, and calculations around
// them.

/** Where a backhaul batch is in its life cycle. */
enum class BackhaulBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A backhaul batch. Its code is unique within a tenant. */
data class BackhaulBatch(
    var code: String,
    var label: String,
    var dock: String,
    var minimum: Double,
    var expiresAt: Int,
    var status: BackhaulBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulBatchStatus): Boolean {
        when (status) {
            BackhaulBatchStatus.OPEN -> {
                when (next) {
                    BackhaulBatchStatus.RESOLVED, BackhaulBatchStatus.REOPENED -> return true
                }
                return false
            }
            BackhaulBatchStatus.IN_REVIEW -> return next == BackhaulBatchStatus.REOPENED
            BackhaulBatchStatus.RESOLVED -> {
                when (next) {
                    BackhaulBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            BackhaulBatchStatus.REOPENED -> {
                when (next) {
                    BackhaulBatchStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BackhaulBatchStatus) {
        if (!canMoveTo(next)) {
            error("backhaul batch $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedVolume(): Double = round((minimum - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): BackhaulBatch {
        return BackhaulBatch(
            code = code,
            label = label,
            dock = dock,
            minimum = minimum,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a backhaul batch in status open. */
fun newBackhaulBatch(code: String, label: String): BackhaulBatch {
    return BackhaulBatch(
        code = code,
        label = label,
        dock = "",
        minimum = 0.0,
        expiresAt = 0,
        status = BackhaulBatchStatus.OPEN,
    )
}
