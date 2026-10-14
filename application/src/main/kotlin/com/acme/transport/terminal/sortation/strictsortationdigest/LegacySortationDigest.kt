package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy sortation digest records of terminal's sortation work, and the rules, stores, and calculations
// around them.

/** Where a legacy sortation digest is in its life cycle. */
enum class LegacySortationDigestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy sortation digest. Its code is unique within a tenant. */
data class LegacySortationDigest(
    var code: String,
    var title: String,
    var locale: String,
    var timezone: String,
    var archived: Boolean,
    var category: String,
    var maximum: Double,
    var quantity: Int,
    var segment: String,
    var batchSize: Int,
    var checksum: String,
    var status: LegacySortationDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacySortationDigestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacySortationDigestStatus): Boolean {
        when (status) {
            LegacySortationDigestStatus.PLANNED -> return next == LegacySortationDigestStatus.IN_PROGRESS
            LegacySortationDigestStatus.SCHEDULED -> {
                when (next) {
                    LegacySortationDigestStatus.COMPLETED -> return true
                }
                return false
            }
            LegacySortationDigestStatus.IN_PROGRESS -> {
                when (next) {
                    LegacySortationDigestStatus.COMPLETED, LegacySortationDigestStatus.CANCELLED -> return true
                }
                return false
            }
            LegacySortationDigestStatus.COMPLETED -> return next == LegacySortationDigestStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacySortationDigestStatus) {
        if (!canMoveTo(next)) {
            error("legacy sortation digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy sortation digest"} $code"

    fun netTotal(): Double = round((maximum + quantity.toDouble()) * 100) / 100

    fun snapshot(): LegacySortationDigest {
        return LegacySortationDigest(
            code = code,
            title = title,
            locale = locale,
            timezone = timezone,
            archived = archived,
            category = category,
            maximum = maximum,
            quantity = quantity,
            segment = segment,
            batchSize = batchSize,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a legacy sortation digest in status planned. */
fun newLegacySortationDigest(code: String, title: String): LegacySortationDigest {
    return LegacySortationDigest(
        code = code,
        title = title,
        locale = "",
        timezone = "",
        archived = false,
        category = "",
        maximum = 0.0,
        quantity = 0,
        segment = "",
        batchSize = 0,
        checksum = "",
        status = LegacySortationDigestStatus.PLANNED,
    )
}
