package com.acme.transport.customs.clearance.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance baseline is in its life cycle. */
enum class ClearanceBaselineStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a clearance baseline and its current status. */
data class ClearanceBaseline(
    var code: String,
    var title: String,
    var name: String,
    var phone: String,
    var lane: String,
    var retries: Int,
    var windowDays: Int,
    var status: ClearanceBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceBaselineStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceBaselineStatus): Boolean {
        when (status) {
            ClearanceBaselineStatus.PLANNED -> return next == ClearanceBaselineStatus.SCHEDULED || next == ClearanceBaselineStatus.CANCELLED
            ClearanceBaselineStatus.SCHEDULED -> {
                when (next) {
                    ClearanceBaselineStatus.IN_PROGRESS, ClearanceBaselineStatus.CANCELLED -> return true
                }
                return false
            }
            ClearanceBaselineStatus.IN_PROGRESS -> {
                when (next) {
                    ClearanceBaselineStatus.COMPLETED, ClearanceBaselineStatus.CANCELLED -> return true
                }
                return false
            }
            ClearanceBaselineStatus.COMPLETED -> return next == ClearanceBaselineStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ClearanceBaselineStatus) {
        if (!canMoveTo(next)) {
            error("clearance baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun netLoad(): Double = round(retries.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): ClearanceBaseline {
        return ClearanceBaseline(
            code = code,
            title = title,
            name = name,
            phone = phone,
            lane = lane,
            retries = retries,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a clearance baseline in status planned. */
fun newClearanceBaseline(code: String, title: String): ClearanceBaseline {
    return ClearanceBaseline(
        code = code,
        title = title,
        name = "",
        phone = "",
        lane = "",
        retries = 0,
        windowDays = 0,
        status = ClearanceBaselineStatus.PLANNED,
    )
}
