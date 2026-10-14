package com.acme.transport.terminal.sortation.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered sortation record records of terminal's sortation work, and the rules, stores, and calculations
// around them.

/** Where a tiered sortation record is in its life cycle. */
enum class TieredSortationRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One tiered sortation record, with what the planners need to act on it. */
data class TieredSortationRecord(
    var code: String,
    var title: String,
    var version: Int,
    var pallets: Int,
    var floorValue: Double,
    var status: TieredSortationRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredSortationRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredSortationRecordStatus): Boolean {
        when (status) {
            TieredSortationRecordStatus.CREATED -> return next == TieredSortationRecordStatus.PUBLISHED
            TieredSortationRecordStatus.VALIDATED -> return next == TieredSortationRecordStatus.PUBLISHED || next == TieredSortationRecordStatus.RETIRED
            TieredSortationRecordStatus.PUBLISHED -> return next == TieredSortationRecordStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TieredSortationRecordStatus) {
        if (!canMoveTo(next)) {
            error("tiered sortation record $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun weightedExposure(): Double = round(pallets.toDouble() * floorValue * 100) / 100

    fun snapshot(): TieredSortationRecord {
        return TieredSortationRecord(
            code = code,
            title = title,
            version = version,
            pallets = pallets,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered sortation record in status created. */
fun newTieredSortationRecord(code: String, title: String): TieredSortationRecord {
    return TieredSortationRecord(
        code = code,
        title = title,
        version = 0,
        pallets = 0,
        floorValue = 0.0,
        status = TieredSortationRecordStatus.CREATED,
    )
}
