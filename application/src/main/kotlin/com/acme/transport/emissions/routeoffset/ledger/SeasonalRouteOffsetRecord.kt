package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal route offset record is in its life cycle. */
enum class SeasonalRouteOffsetRecordStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One seasonal route offset record, with what the planners need to act on it. */
data class SeasonalRouteOffsetRecord(
    var code: String,
    var name: String,
    var quantity: Int,
    var weight: Double,
    var source: String,
    var checksum: String,
    var stopCount: Int,
    var reviewer: String,
    var pallets: Int,
    var trailerId: String,
    var status: SeasonalRouteOffsetRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRouteOffsetRecordStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRouteOffsetRecordStatus): Boolean {
        when (status) {
            SeasonalRouteOffsetRecordStatus.IDLE -> return next == SeasonalRouteOffsetRecordStatus.READY
            SeasonalRouteOffsetRecordStatus.WARMING -> return next == SeasonalRouteOffsetRecordStatus.READY || next == SeasonalRouteOffsetRecordStatus.DRAINING
            SeasonalRouteOffsetRecordStatus.READY -> return next == SeasonalRouteOffsetRecordStatus.DRAINING
            SeasonalRouteOffsetRecordStatus.DRAINING -> return next == SeasonalRouteOffsetRecordStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: SeasonalRouteOffsetRecordStatus) {
        if (!canMoveTo(next)) {
            error("seasonal route offset record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun netTotal(): Double = round((weight + stopCount.toDouble()) * 100) / 100

    fun snapshot(): SeasonalRouteOffsetRecord {
        return SeasonalRouteOffsetRecord(
            code = code,
            name = name,
            quantity = quantity,
            weight = weight,
            source = source,
            checksum = checksum,
            stopCount = stopCount,
            reviewer = reviewer,
            pallets = pallets,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a seasonal route offset record in status idle. */
fun newSeasonalRouteOffsetRecord(code: String, name: String): SeasonalRouteOffsetRecord {
    return SeasonalRouteOffsetRecord(
        code = code,
        name = name,
        quantity = 0,
        weight = 0.0,
        source = "",
        checksum = "",
        stopCount = 0,
        reviewer = "",
        pallets = 0,
        trailerId = "",
        status = SeasonalRouteOffsetRecordStatus.IDLE,
    )
}
