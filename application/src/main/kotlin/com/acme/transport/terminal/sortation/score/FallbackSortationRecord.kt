package com.acme.transport.terminal.sortation.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback sortation record is in its life cycle. */
enum class FallbackSortationRecordStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A fallback sortation record as the sortation team keeps it. */
data class FallbackSortationRecord(
    var code: String,
    var label: String,
    var limit: Int,
    var notes: String,
    var durationMinutes: Int,
    var threshold: Double,
    var stopCount: Int,
    var status: FallbackSortationRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackSortationRecordStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackSortationRecordStatus): Boolean {
        when (status) {
            FallbackSortationRecordStatus.DRAFT -> {
                when (next) {
                    FallbackSortationRecordStatus.ACTIVE, FallbackSortationRecordStatus.CLOSED -> return true
                }
                return false
            }
            FallbackSortationRecordStatus.PENDING -> {
                when (next) {
                    FallbackSortationRecordStatus.CLOSED -> return true
                }
                return false
            }
            FallbackSortationRecordStatus.ACTIVE -> return next == FallbackSortationRecordStatus.SUSPENDED || next == FallbackSortationRecordStatus.CLOSED
            FallbackSortationRecordStatus.SUSPENDED -> return next == FallbackSortationRecordStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FallbackSortationRecordStatus) {
        if (!canMoveTo(next)) {
            error("fallback sortation record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedValue(): Double = round((durationMinutes.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): FallbackSortationRecord {
        return FallbackSortationRecord(
            code = code,
            label = label,
            limit = limit,
            notes = notes,
            durationMinutes = durationMinutes,
            threshold = threshold,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback sortation record in status draft. */
fun newFallbackSortationRecord(code: String, label: String): FallbackSortationRecord {
    return FallbackSortationRecord(
        code = code,
        label = label,
        limit = 0,
        notes = "",
        durationMinutes = 0,
        threshold = 0.0,
        stopCount = 0,
        status = FallbackSortationRecordStatus.DRAFT,
    )
}
