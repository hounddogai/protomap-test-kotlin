package com.acme.transport.linehaul.relay.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient relay notice is in its life cycle. */
enum class LenientRelayNoticeStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One lenient relay notice, with what the planners need to act on it. */
data class LenientRelayNotice(
    var code: String,
    var label: String,
    var phone: String,
    var carrier: String,
    var approved: Boolean,
    var maximum: Double,
    var segment: String,
    var rate: Double,
    var country: String,
    var retries: Int,
    var locale: String,
    var status: LenientRelayNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRelayNoticeStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRelayNoticeStatus): Boolean {
        when (status) {
            LenientRelayNoticeStatus.NEW -> {
                when (next) {
                    LenientRelayNoticeStatus.QUEUED, LenientRelayNoticeStatus.RUNNING -> return true
                }
                return false
            }
            LenientRelayNoticeStatus.QUEUED -> {
                when (next) {
                    LenientRelayNoticeStatus.RUNNING -> return true
                }
                return false
            }
            LenientRelayNoticeStatus.RUNNING -> {
                when (next) {
                    LenientRelayNoticeStatus.DONE, LenientRelayNoticeStatus.FAILED -> return true
                }
                return false
            }
            LenientRelayNoticeStatus.DONE -> {
                when (next) {
                    LenientRelayNoticeStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientRelayNoticeStatus) {
        if (!canMoveTo(next)) {
            error("lenient relay notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveLoad(): Double = round((rate + retries.toDouble()) * 100) / 100

    fun snapshot(): LenientRelayNotice {
        return LenientRelayNotice(
            code = code,
            label = label,
            phone = phone,
            carrier = carrier,
            approved = approved,
            maximum = maximum,
            segment = segment,
            rate = rate,
            country = country,
            retries = retries,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a lenient relay notice in status new. */
fun newLenientRelayNotice(code: String, label: String): LenientRelayNotice {
    return LenientRelayNotice(
        code = code,
        label = label,
        phone = "",
        carrier = "",
        approved = false,
        maximum = 0.0,
        segment = "",
        rate = 0.0,
        country = "",
        retries = 0,
        locale = "",
        status = LenientRelayNoticeStatus.NEW,
    )
}
