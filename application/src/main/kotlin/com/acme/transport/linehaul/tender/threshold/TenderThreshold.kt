package com.acme.transport.linehaul.tender.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tender threshold records of linehaul's tender work, and the rules, stores, and calculations around
// them.

/** Where a tender threshold is in its life cycle. */
enum class TenderThresholdStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tender threshold as the tender team keeps it. */
data class TenderThreshold(
    var code: String,
    var name: String,
    var timezone: String,
    var checksum: String,
    var limit: Int,
    var ceiling: Double,
    var reviewer: String,
    var stopCount: Int,
    var status: TenderThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderThresholdStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderThresholdStatus): Boolean {
        when (status) {
            TenderThresholdStatus.CREATED -> return next == TenderThresholdStatus.PUBLISHED || next == TenderThresholdStatus.RETIRED
            TenderThresholdStatus.VALIDATED -> return next == TenderThresholdStatus.PUBLISHED
            TenderThresholdStatus.PUBLISHED -> return next == TenderThresholdStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TenderThresholdStatus) {
        if (!canMoveTo(next)) {
            error("tender threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveLoad(): Double = round((ceiling - limit.toDouble()) * 100) / 100

    fun snapshot(): TenderThreshold {
        return TenderThreshold(
            code = code,
            name = name,
            timezone = timezone,
            checksum = checksum,
            limit = limit,
            ceiling = ceiling,
            reviewer = reviewer,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tender threshold in status created. */
fun newTenderThreshold(code: String, name: String): TenderThreshold {
    return TenderThreshold(
        code = code,
        name = name,
        timezone = "",
        checksum = "",
        limit = 0,
        ceiling = 0.0,
        reviewer = "",
        stopCount = 0,
        status = TenderThresholdStatus.CREATED,
    )
}
