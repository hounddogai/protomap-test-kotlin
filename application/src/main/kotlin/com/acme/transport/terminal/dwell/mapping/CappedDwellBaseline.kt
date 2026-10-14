package com.acme.transport.terminal.dwell.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped dwell baseline is in its life cycle. */
enum class CappedDwellBaselineStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a capped dwell baseline and its current status. */
data class CappedDwellBaseline(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var loadKg: Double,
    var dock: String,
    var floorValue: Double,
    var status: CappedDwellBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDwellBaselineStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDwellBaselineStatus): Boolean {
        when (status) {
            CappedDwellBaselineStatus.IDLE -> {
                when (next) {
                    CappedDwellBaselineStatus.DRAINING -> return true
                }
                return false
            }
            CappedDwellBaselineStatus.WARMING -> {
                when (next) {
                    CappedDwellBaselineStatus.DRAINING, CappedDwellBaselineStatus.STOPPED -> return true
                }
                return false
            }
            CappedDwellBaselineStatus.READY -> return next == CappedDwellBaselineStatus.DRAINING || next == CappedDwellBaselineStatus.STOPPED
            CappedDwellBaselineStatus.DRAINING -> {
                when (next) {
                    CappedDwellBaselineStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedDwellBaselineStatus) {
        if (!canMoveTo(next)) {
            error("capped dwell baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netTotal(): Double = round((floorValue - fuelLiters) * 100) / 100

    fun snapshot(): CappedDwellBaseline {
        return CappedDwellBaseline(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            loadKg = loadKg,
            dock = dock,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped dwell baseline in status idle. */
fun newCappedDwellBaseline(code: String, label: String): CappedDwellBaseline {
    return CappedDwellBaseline(
        code = code,
        label = label,
        fuelLiters = 0.0,
        loadKg = 0.0,
        dock = "",
        floorValue = 0.0,
        status = CappedDwellBaselineStatus.IDLE,
    )
}
