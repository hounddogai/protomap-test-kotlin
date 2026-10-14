package com.acme.transport.terminal.sortation.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary sortation batch is in its life cycle. */
enum class PrimarySortationBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A primary sortation batch. Its code is unique within a tenant. */
data class PrimarySortationBatch(
    var code: String,
    var title: String,
    var retries: Int,
    var locale: String,
    var margin: Double,
    var timezone: String,
    var reviewer: String,
    var contactEmail: String,
    var loadKg: Double,
    var maximum: Double,
    var label: String,
    var status: PrimarySortationBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimarySortationBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimarySortationBatchStatus): Boolean {
        when (status) {
            PrimarySortationBatchStatus.DRAFT -> return next == PrimarySortationBatchStatus.CLOSED
            PrimarySortationBatchStatus.PENDING -> return next == PrimarySortationBatchStatus.SUSPENDED
            PrimarySortationBatchStatus.ACTIVE -> return next == PrimarySortationBatchStatus.SUSPENDED || next == PrimarySortationBatchStatus.CLOSED
            PrimarySortationBatchStatus.SUSPENDED -> {
                when (next) {
                    PrimarySortationBatchStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimarySortationBatchStatus) {
        if (!canMoveTo(next)) {
            error("primary sortation batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary sortation batch"} $code"

    fun adjustedValue(): Double = round((maximum - retries.toDouble()) * 100) / 100

    fun snapshot(): PrimarySortationBatch {
        return PrimarySortationBatch(
            code = code,
            title = title,
            retries = retries,
            locale = locale,
            margin = margin,
            timezone = timezone,
            reviewer = reviewer,
            contactEmail = contactEmail,
            loadKg = loadKg,
            maximum = maximum,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a primary sortation batch in status draft. */
fun newPrimarySortationBatch(code: String, title: String): PrimarySortationBatch {
    return PrimarySortationBatch(
        code = code,
        title = title,
        retries = 0,
        locale = "",
        margin = 0.0,
        timezone = "",
        reviewer = "",
        contactEmail = "",
        loadKg = 0.0,
        maximum = 0.0,
        label = "",
        status = PrimarySortationBatchStatus.DRAFT,
    )
}
