package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door adjustment is in its life cycle. */
enum class DockDoorAdjustmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A dock door adjustment as the dock door team keeps it. */
data class DockDoorAdjustment(
    var code: String,
    var title: String,
    var quantity: Int,
    var cost: Double,
    var limit: Int,
    var description: String,
    var status: DockDoorAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorAdjustmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorAdjustmentStatus): Boolean {
        when (status) {
            DockDoorAdjustmentStatus.PLANNED -> {
                when (next) {
                    DockDoorAdjustmentStatus.COMPLETED, DockDoorAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
            DockDoorAdjustmentStatus.SCHEDULED -> return next == DockDoorAdjustmentStatus.IN_PROGRESS
            DockDoorAdjustmentStatus.IN_PROGRESS -> {
                when (next) {
                    DockDoorAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
            DockDoorAdjustmentStatus.COMPLETED -> return next == DockDoorAdjustmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DockDoorAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("dock door adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"dock door adjustment"} $code"

    fun netTotal(): Double = round((cost + limit.toDouble()) * 100) / 100

    fun snapshot(): DockDoorAdjustment {
        return DockDoorAdjustment(
            code = code,
            title = title,
            quantity = quantity,
            cost = cost,
            limit = limit,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a dock door adjustment in status planned. */
fun newDockDoorAdjustment(code: String, title: String): DockDoorAdjustment {
    return DockDoorAdjustment(
        code = code,
        title = title,
        quantity = 0,
        cost = 0.0,
        limit = 0,
        description = "",
        status = DockDoorAdjustmentStatus.PLANNED,
    )
}
