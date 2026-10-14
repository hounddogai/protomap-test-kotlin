package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dock door variance records of terminal's dock door work, and the rules, stores, and calculations around
// them.

/** Where a dock door variance is in its life cycle. */
enum class DockDoorVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a dock door variance and its current status. */
data class DockDoorVariance(
    var code: String,
    var label: String,
    var contactEmail: String,
    var windowDays: Int,
    var batchSize: Int,
    var description: String,
    var unit: String,
    var notes: String,
    var expiresAt: Int,
    var lane: String,
    var ratio: Double,
    var status: DockDoorVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorVarianceStatus): Boolean {
        when (status) {
            DockDoorVarianceStatus.IDLE -> return next == DockDoorVarianceStatus.READY || next == DockDoorVarianceStatus.STOPPED
            DockDoorVarianceStatus.WARMING -> {
                when (next) {
                    DockDoorVarianceStatus.DRAINING -> return true
                }
                return false
            }
            DockDoorVarianceStatus.READY -> return next == DockDoorVarianceStatus.DRAINING || next == DockDoorVarianceStatus.STOPPED
            DockDoorVarianceStatus.DRAINING -> return next == DockDoorVarianceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DockDoorVarianceStatus) {
        if (!canMoveTo(next)) {
            error("dock door variance $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun netVolume(): Double = round(expiresAt.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): DockDoorVariance {
        return DockDoorVariance(
            code = code,
            label = label,
            contactEmail = contactEmail,
            windowDays = windowDays,
            batchSize = batchSize,
            description = description,
            unit = unit,
            notes = notes,
            expiresAt = expiresAt,
            lane = lane,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a dock door variance in status idle. */
fun newDockDoorVariance(code: String, label: String): DockDoorVariance {
    return DockDoorVariance(
        code = code,
        label = label,
        contactEmail = "",
        windowDays = 0,
        batchSize = 0,
        description = "",
        unit = "",
        notes = "",
        expiresAt = 0,
        lane = "",
        ratio = 0.0,
        status = DockDoorVarianceStatus.IDLE,
    )
}
