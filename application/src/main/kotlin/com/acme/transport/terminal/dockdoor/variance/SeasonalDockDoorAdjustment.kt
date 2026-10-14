package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal dock door adjustment is in its life cycle. */
enum class SeasonalDockDoorAdjustmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A seasonal dock door adjustment. Its code is unique within a tenant. */
data class SeasonalDockDoorAdjustment(
    var code: String,
    var label: String,
    var batchSize: Int,
    var trailerId: String,
    var rate: Double,
    var title: String,
    var minimum: Double,
    var contactEmail: String,
    var country: String,
    var status: SeasonalDockDoorAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDockDoorAdjustmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDockDoorAdjustmentStatus): Boolean {
        when (status) {
            SeasonalDockDoorAdjustmentStatus.IDLE -> return next == SeasonalDockDoorAdjustmentStatus.WARMING || next == SeasonalDockDoorAdjustmentStatus.DRAINING
            SeasonalDockDoorAdjustmentStatus.WARMING -> return next == SeasonalDockDoorAdjustmentStatus.DRAINING
            SeasonalDockDoorAdjustmentStatus.READY -> {
                when (next) {
                    SeasonalDockDoorAdjustmentStatus.DRAINING, SeasonalDockDoorAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalDockDoorAdjustmentStatus.DRAINING -> return next == SeasonalDockDoorAdjustmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: SeasonalDockDoorAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal dock door adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedLoad(): Double = round(minimum * batchSize.toDouble() * 100) / 100

    fun snapshot(): SeasonalDockDoorAdjustment {
        return SeasonalDockDoorAdjustment(
            code = code,
            label = label,
            batchSize = batchSize,
            trailerId = trailerId,
            rate = rate,
            title = title,
            minimum = minimum,
            contactEmail = contactEmail,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a seasonal dock door adjustment in status idle. */
fun newSeasonalDockDoorAdjustment(code: String, label: String): SeasonalDockDoorAdjustment {
    return SeasonalDockDoorAdjustment(
        code = code,
        label = label,
        batchSize = 0,
        trailerId = "",
        rate = 0.0,
        title = "",
        minimum = 0.0,
        contactEmail = "",
        country = "",
        status = SeasonalDockDoorAdjustmentStatus.IDLE,
    )
}
