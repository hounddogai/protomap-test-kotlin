package com.acme.transport.linehaul.relay.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy relay notice is in its life cycle. */
enum class LegacyRelayNoticeStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy relay notice as the relay team keeps it. */
data class LegacyRelayNotice(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var amount: Double,
    var threshold: Double,
    var trailerId: String,
    var timezone: String,
    var locale: String,
    var startsAt: Int,
    var phone: String,
    var status: LegacyRelayNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyRelayNoticeStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyRelayNoticeStatus): Boolean {
        when (status) {
            LegacyRelayNoticeStatus.NEW -> return next == LegacyRelayNoticeStatus.RUNNING
            LegacyRelayNoticeStatus.QUEUED -> {
                when (next) {
                    LegacyRelayNoticeStatus.FAILED -> return true
                }
                return false
            }
            LegacyRelayNoticeStatus.RUNNING -> {
                when (next) {
                    LegacyRelayNoticeStatus.DONE, LegacyRelayNoticeStatus.FAILED -> return true
                }
                return false
            }
            LegacyRelayNoticeStatus.DONE -> {
                when (next) {
                    LegacyRelayNoticeStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyRelayNoticeStatus) {
        if (!canMoveTo(next)) {
            error("legacy relay notice $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"legacy relay notice"} $code"

    fun adjustedVolume(): Double = round((amount + threshold) * 100) / 100

    fun snapshot(): LegacyRelayNotice {
        return LegacyRelayNotice(
            code = code,
            title = title,
            expiresAt = expiresAt,
            amount = amount,
            threshold = threshold,
            trailerId = trailerId,
            timezone = timezone,
            locale = locale,
            startsAt = startsAt,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a legacy relay notice in status new. */
fun newLegacyRelayNotice(code: String, title: String): LegacyRelayNotice {
    return LegacyRelayNotice(
        code = code,
        title = title,
        expiresAt = 0,
        amount = 0.0,
        threshold = 0.0,
        trailerId = "",
        timezone = "",
        locale = "",
        startsAt = 0,
        phone = "",
        status = LegacyRelayNoticeStatus.NEW,
    )
}
