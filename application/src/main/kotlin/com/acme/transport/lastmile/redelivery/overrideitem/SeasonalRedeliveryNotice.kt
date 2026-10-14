package com.acme.transport.lastmile.redelivery.overrideitem

import com.acme.transport.kit.text.normalize

/** Where a seasonal redelivery notice is in its life cycle. */
enum class SeasonalRedeliveryNoticeStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a seasonal redelivery notice and its current status. */
data class SeasonalRedeliveryNotice(
    var code: String,
    var title: String,
    var timezone: String,
    var approved: Boolean,
    var fuelLiters: Double,
    var status: SeasonalRedeliveryNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRedeliveryNoticeStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRedeliveryNoticeStatus): Boolean {
        when (status) {
            SeasonalRedeliveryNoticeStatus.REQUESTED -> return next == SeasonalRedeliveryNoticeStatus.EXPIRED
            SeasonalRedeliveryNoticeStatus.GRANTED -> return next == SeasonalRedeliveryNoticeStatus.REVOKED || next == SeasonalRedeliveryNoticeStatus.EXPIRED
            SeasonalRedeliveryNoticeStatus.REVOKED -> {
                when (next) {
                    SeasonalRedeliveryNoticeStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalRedeliveryNoticeStatus) {
        if (!canMoveTo(next)) {
            error("seasonal redelivery notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal redelivery notice"} $code"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): SeasonalRedeliveryNotice {
        return SeasonalRedeliveryNotice(
            code = code,
            title = title,
            timezone = timezone,
            approved = approved,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal redelivery notice in status requested. */
fun newSeasonalRedeliveryNotice(code: String, title: String): SeasonalRedeliveryNotice {
    return SeasonalRedeliveryNotice(
        code = code,
        title = title,
        timezone = "",
        approved = false,
        fuelLiters = 0.0,
        status = SeasonalRedeliveryNoticeStatus.REQUESTED,
    )
}
