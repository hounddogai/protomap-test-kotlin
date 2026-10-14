package com.acme.transport.terminal.sortation.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation threshold is in its life cycle. */
enum class SortationThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a sortation threshold and its current status. */
data class SortationThreshold(
    var code: String,
    var label: String,
    var dock: String,
    var score: Double,
    var retries: Int,
    var status: SortationThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationThresholdStatus): Boolean {
        when (status) {
            SortationThresholdStatus.PROPOSED -> {
                when (next) {
                    SortationThresholdStatus.APPROVED, SortationThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
            SortationThresholdStatus.APPROVED -> return next == SortationThresholdStatus.REJECTED || next == SortationThresholdStatus.WITHDRAWN
            SortationThresholdStatus.REJECTED -> return next == SortationThresholdStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: SortationThresholdStatus) {
        if (!canMoveTo(next)) {
            error("sortation threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netTotal(): Double = round(score * retries.toDouble() * 100) / 100

    fun snapshot(): SortationThreshold {
        return SortationThreshold(
            code = code,
            label = label,
            dock = dock,
            score = score,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a sortation threshold in status proposed. */
fun newSortationThreshold(code: String, label: String): SortationThreshold {
    return SortationThreshold(
        code = code,
        label = label,
        dock = "",
        score = 0.0,
        retries = 0,
        status = SortationThresholdStatus.PROPOSED,
    )
}
