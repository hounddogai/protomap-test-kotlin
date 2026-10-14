package com.acme.transport.lastmile.drop.dropallocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient drop notice is in its life cycle. */
enum class LenientDropNoticeStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A lenient drop notice as the drop team keeps it. */
data class LenientDropNotice(
    var code: String,
    var label: String,
    var lane: String,
    var category: String,
    var graceDays: Int,
    var retries: Int,
    var status: LenientDropNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDropNoticeStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDropNoticeStatus): Boolean {
        when (status) {
            LenientDropNoticeStatus.CREATED -> {
                when (next) {
                    LenientDropNoticeStatus.PUBLISHED -> return true
                }
                return false
            }
            LenientDropNoticeStatus.VALIDATED -> {
                when (next) {
                    LenientDropNoticeStatus.PUBLISHED -> return true
                }
                return false
            }
            LenientDropNoticeStatus.PUBLISHED -> return next == LenientDropNoticeStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientDropNoticeStatus) {
        if (!canMoveTo(next)) {
            error("lenient drop notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"lenient drop notice"} $code"

    fun effectiveValue(): Double = round((graceDays.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): LenientDropNotice {
        return LenientDropNotice(
            code = code,
            label = label,
            lane = lane,
            category = category,
            graceDays = graceDays,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a lenient drop notice in status created. */
fun newLenientDropNotice(code: String, label: String): LenientDropNotice {
    return LenientDropNotice(
        code = code,
        label = label,
        lane = "",
        category = "",
        graceDays = 0,
        retries = 0,
        status = LenientDropNoticeStatus.CREATED,
    )
}
