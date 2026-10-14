package com.acme.transport.network.transittime.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered transit time window is in its life cycle. */
enum class TieredTransitTimeWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a tiered transit time window and its current status. */
data class TieredTransitTimeWindow(
    var code: String,
    var label: String,
    var margin: Double,
    var ownerId: String,
    var visible: Boolean,
    var locale: String,
    var checksum: String,
    var floorValue: Double,
    var summary: String,
    var phone: String,
    var status: TieredTransitTimeWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTransitTimeWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTransitTimeWindowStatus): Boolean {
        when (status) {
            TieredTransitTimeWindowStatus.DRAFT -> {
                when (next) {
                    TieredTransitTimeWindowStatus.ACTIVE, TieredTransitTimeWindowStatus.SUSPENDED -> return true
                }
                return false
            }
            TieredTransitTimeWindowStatus.PENDING -> {
                when (next) {
                    TieredTransitTimeWindowStatus.ACTIVE, TieredTransitTimeWindowStatus.CLOSED -> return true
                }
                return false
            }
            TieredTransitTimeWindowStatus.ACTIVE -> return next == TieredTransitTimeWindowStatus.SUSPENDED || next == TieredTransitTimeWindowStatus.CLOSED
            TieredTransitTimeWindowStatus.SUSPENDED -> return next == TieredTransitTimeWindowStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TieredTransitTimeWindowStatus) {
        if (!canMoveTo(next)) {
            error("tiered transit time window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tiered transit time window"} $code"

    fun projectedExposure(): Double = round((margin + floorValue) * 100) / 100

    fun snapshot(): TieredTransitTimeWindow {
        return TieredTransitTimeWindow(
            code = code,
            label = label,
            margin = margin,
            ownerId = ownerId,
            visible = visible,
            locale = locale,
            checksum = checksum,
            floorValue = floorValue,
            summary = summary,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tiered transit time window in status draft. */
fun newTieredTransitTimeWindow(code: String, label: String): TieredTransitTimeWindow {
    return TieredTransitTimeWindow(
        code = code,
        label = label,
        margin = 0.0,
        ownerId = "",
        visible = false,
        locale = "",
        checksum = "",
        floorValue = 0.0,
        summary = "",
        phone = "",
        status = TieredTransitTimeWindowStatus.DRAFT,
    )
}
