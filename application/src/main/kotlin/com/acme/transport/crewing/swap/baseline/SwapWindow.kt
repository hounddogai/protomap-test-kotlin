package com.acme.transport.crewing.swap.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap window is in its life cycle. */
enum class SwapWindowStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a swap window and its current status. */
data class SwapWindow(
    var code: String,
    var label: String,
    var priority: Int,
    var summary: String,
    var vehicleId: String,
    var createdAt: Int,
    var region: String,
    var durationMinutes: Int,
    var timezone: String,
    var status: SwapWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapWindowStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapWindowStatus): Boolean {
        when (status) {
            SwapWindowStatus.CREATED -> {
                when (next) {
                    SwapWindowStatus.VALIDATED -> return true
                }
                return false
            }
            SwapWindowStatus.VALIDATED -> return next == SwapWindowStatus.RETIRED
            SwapWindowStatus.PUBLISHED -> {
                when (next) {
                    SwapWindowStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapWindowStatus) {
        if (!canMoveTo(next)) {
            error("swap window $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedLoad(): Double = round((createdAt.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): SwapWindow {
        return SwapWindow(
            code = code,
            label = label,
            priority = priority,
            summary = summary,
            vehicleId = vehicleId,
            createdAt = createdAt,
            region = region,
            durationMinutes = durationMinutes,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a swap window in status created. */
fun newSwapWindow(code: String, label: String): SwapWindow {
    return SwapWindow(
        code = code,
        label = label,
        priority = 0,
        summary = "",
        vehicleId = "",
        createdAt = 0,
        region = "",
        durationMinutes = 0,
        timezone = "",
        status = SwapWindowStatus.CREATED,
    )
}
