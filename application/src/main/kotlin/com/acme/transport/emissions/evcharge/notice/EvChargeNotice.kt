package com.acme.transport.emissions.evcharge.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The ev charge notice records of emissions's ev charge work, and the rules, stores, and calculations around
// them.

/** Where a ev charge notice is in its life cycle. */
enum class EvChargeNoticeStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One ev charge notice, with what the planners need to act on it. */
data class EvChargeNotice(
    var code: String,
    var name: String,
    var archived: Boolean,
    var ceiling: Double,
    var cost: Double,
    var status: EvChargeNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeNoticeStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeNoticeStatus): Boolean {
        when (status) {
            EvChargeNoticeStatus.REQUESTED -> return next == EvChargeNoticeStatus.GRANTED || next == EvChargeNoticeStatus.EXPIRED
            EvChargeNoticeStatus.GRANTED -> return next == EvChargeNoticeStatus.EXPIRED
            EvChargeNoticeStatus.REVOKED -> return next == EvChargeNoticeStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: EvChargeNoticeStatus) {
        if (!canMoveTo(next)) {
            error("ev charge notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedLoad(): Double = round((ceiling + cost) * 100) / 100

    fun snapshot(): EvChargeNotice {
        return EvChargeNotice(
            code = code,
            name = name,
            archived = archived,
            ceiling = ceiling,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a ev charge notice in status requested. */
fun newEvChargeNotice(code: String, name: String): EvChargeNotice {
    return EvChargeNotice(
        code = code,
        name = name,
        archived = false,
        ceiling = 0.0,
        cost = 0.0,
        status = EvChargeNoticeStatus.REQUESTED,
    )
}
