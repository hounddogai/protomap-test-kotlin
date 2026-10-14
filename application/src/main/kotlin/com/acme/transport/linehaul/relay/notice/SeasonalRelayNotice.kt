package com.acme.transport.linehaul.relay.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal relay notice records of linehaul's relay work, and the rules, stores, and calculations around
// them.

/** Where a seasonal relay notice is in its life cycle. */
enum class SeasonalRelayNoticeStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a seasonal relay notice and its current status. */
data class SeasonalRelayNotice(
    var code: String,
    var label: String,
    var discount: Double,
    var carrier: String,
    var threshold: Double,
    var floorValue: Double,
    var status: SeasonalRelayNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRelayNoticeStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRelayNoticeStatus): Boolean {
        when (status) {
            SeasonalRelayNoticeStatus.CREATED -> return next == SeasonalRelayNoticeStatus.VALIDATED || next == SeasonalRelayNoticeStatus.RETIRED
            SeasonalRelayNoticeStatus.VALIDATED -> return next == SeasonalRelayNoticeStatus.PUBLISHED || next == SeasonalRelayNoticeStatus.RETIRED
            SeasonalRelayNoticeStatus.PUBLISHED -> {
                when (next) {
                    SeasonalRelayNoticeStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalRelayNoticeStatus) {
        if (!canMoveTo(next)) {
            error("seasonal relay notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedVolume(): Double = round(discount * floorValue * 100) / 100

    fun snapshot(): SeasonalRelayNotice {
        return SeasonalRelayNotice(
            code = code,
            label = label,
            discount = discount,
            carrier = carrier,
            threshold = threshold,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal relay notice in status created. */
fun newSeasonalRelayNotice(code: String, label: String): SeasonalRelayNotice {
    return SeasonalRelayNotice(
        code = code,
        label = label,
        discount = 0.0,
        carrier = "",
        threshold = 0.0,
        floorValue = 0.0,
        status = SeasonalRelayNoticeStatus.CREATED,
    )
}
