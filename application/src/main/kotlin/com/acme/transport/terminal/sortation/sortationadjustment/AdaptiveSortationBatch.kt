package com.acme.transport.terminal.sortation.sortationadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive sortation batch is in its life cycle. */
enum class AdaptiveSortationBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A adaptive sortation batch as the sortation team keeps it. */
data class AdaptiveSortationBatch(
    var code: String,
    var label: String,
    var carrier: String,
    var createdAt: Int,
    var timezone: String,
    var tags: MutableList<String>,
    var threshold: Double,
    var status: AdaptiveSortationBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveSortationBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveSortationBatchStatus): Boolean {
        when (status) {
            AdaptiveSortationBatchStatus.REQUESTED -> {
                when (next) {
                    AdaptiveSortationBatchStatus.GRANTED, AdaptiveSortationBatchStatus.REVOKED -> return true
                }
                return false
            }
            AdaptiveSortationBatchStatus.GRANTED -> return next == AdaptiveSortationBatchStatus.REVOKED || next == AdaptiveSortationBatchStatus.EXPIRED
            AdaptiveSortationBatchStatus.REVOKED -> {
                when (next) {
                    AdaptiveSortationBatchStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveSortationBatchStatus) {
        if (!canMoveTo(next)) {
            error("adaptive sortation batch $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedValue(): Double = round((createdAt.toDouble() - threshold) * 100) / 100

    fun snapshot(): AdaptiveSortationBatch {
        return AdaptiveSortationBatch(
            code = code,
            label = label,
            carrier = carrier,
            createdAt = createdAt,
            timezone = timezone,
            tags = tags,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive sortation batch in status requested. */
fun newAdaptiveSortationBatch(code: String, label: String): AdaptiveSortationBatch {
    return AdaptiveSortationBatch(
        code = code,
        label = label,
        carrier = "",
        createdAt = 0,
        timezone = "",
        tags = mutableListOf<String>(),
        threshold = 0.0,
        status = AdaptiveSortationBatchStatus.REQUESTED,
    )
}
