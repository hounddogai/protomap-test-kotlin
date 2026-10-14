package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal accessorial allocation is in its life cycle. */
enum class SeasonalAccessorialAllocationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal accessorial allocation. Its code is unique within a tenant. */
data class SeasonalAccessorialAllocation(
    var code: String,
    var label: String,
    var ceiling: Double,
    var summary: String,
    var amount: Double,
    var margin: Double,
    var endsAt: Int,
    var visible: Boolean,
    var carrier: String,
    var trailerId: String,
    var stopCount: Int,
    var status: SeasonalAccessorialAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalAccessorialAllocationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalAccessorialAllocationStatus): Boolean {
        when (status) {
            SeasonalAccessorialAllocationStatus.NEW -> {
                when (next) {
                    SeasonalAccessorialAllocationStatus.DONE -> return true
                }
                return false
            }
            SeasonalAccessorialAllocationStatus.QUEUED -> {
                when (next) {
                    SeasonalAccessorialAllocationStatus.RUNNING -> return true
                }
                return false
            }
            SeasonalAccessorialAllocationStatus.RUNNING -> return next == SeasonalAccessorialAllocationStatus.DONE || next == SeasonalAccessorialAllocationStatus.FAILED
            SeasonalAccessorialAllocationStatus.DONE -> {
                when (next) {
                    SeasonalAccessorialAllocationStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalAccessorialAllocationStatus) {
        if (!canMoveTo(next)) {
            error("seasonal accessorial allocation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun netExposure(): Double = round((endsAt.toDouble() - margin) * 100) / 100

    fun snapshot(): SeasonalAccessorialAllocation {
        return SeasonalAccessorialAllocation(
            code = code,
            label = label,
            ceiling = ceiling,
            summary = summary,
            amount = amount,
            margin = margin,
            endsAt = endsAt,
            visible = visible,
            carrier = carrier,
            trailerId = trailerId,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal accessorial allocation in status new. */
fun newSeasonalAccessorialAllocation(code: String, label: String): SeasonalAccessorialAllocation {
    return SeasonalAccessorialAllocation(
        code = code,
        label = label,
        ceiling = 0.0,
        summary = "",
        amount = 0.0,
        margin = 0.0,
        endsAt = 0,
        visible = false,
        carrier = "",
        trailerId = "",
        stopCount = 0,
        status = SeasonalAccessorialAllocationStatus.NEW,
    )
}
