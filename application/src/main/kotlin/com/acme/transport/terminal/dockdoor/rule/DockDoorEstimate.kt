package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door estimate is in its life cycle. */
enum class DockDoorEstimateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a dock door estimate and its current status. */
data class DockDoorEstimate(
    var code: String,
    var title: String,
    var threshold: Double,
    var retries: Int,
    var lane: String,
    var margin: Double,
    var floorValue: Double,
    var checksum: String,
    var status: DockDoorEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorEstimateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorEstimateStatus): Boolean {
        when (status) {
            DockDoorEstimateStatus.REQUESTED -> return next == DockDoorEstimateStatus.REVOKED
            DockDoorEstimateStatus.GRANTED -> {
                when (next) {
                    DockDoorEstimateStatus.REVOKED -> return true
                }
                return false
            }
            DockDoorEstimateStatus.REVOKED -> {
                when (next) {
                    DockDoorEstimateStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DockDoorEstimateStatus) {
        if (!canMoveTo(next)) {
            error("dock door estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun netValue(): Double = round((floorValue + retries.toDouble()) * 100) / 100

    fun snapshot(): DockDoorEstimate {
        return DockDoorEstimate(
            code = code,
            title = title,
            threshold = threshold,
            retries = retries,
            lane = lane,
            margin = margin,
            floorValue = floorValue,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a dock door estimate in status requested. */
fun newDockDoorEstimate(code: String, title: String): DockDoorEstimate {
    return DockDoorEstimate(
        code = code,
        title = title,
        threshold = 0.0,
        retries = 0,
        lane = "",
        margin = 0.0,
        floorValue = 0.0,
        checksum = "",
        status = DockDoorEstimateStatus.REQUESTED,
    )
}
