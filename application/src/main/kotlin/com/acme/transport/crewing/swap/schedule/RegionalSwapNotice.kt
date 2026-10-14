package com.acme.transport.crewing.swap.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional swap notice is in its life cycle. */
enum class RegionalSwapNoticeStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One regional swap notice, with what the planners need to act on it. */
data class RegionalSwapNotice(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var startsAt: Int,
    var ownerId: String,
    var margin: Double,
    var attributes: MutableMap<String, String>,
    var amount: Double,
    var notes: String,
    var attempts: Int,
    var status: RegionalSwapNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalSwapNoticeStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalSwapNoticeStatus): Boolean {
        when (status) {
            RegionalSwapNoticeStatus.REQUESTED -> {
                when (next) {
                    RegionalSwapNoticeStatus.GRANTED, RegionalSwapNoticeStatus.EXPIRED -> return true
                }
                return false
            }
            RegionalSwapNoticeStatus.GRANTED -> return next == RegionalSwapNoticeStatus.REVOKED || next == RegionalSwapNoticeStatus.EXPIRED
            RegionalSwapNoticeStatus.REVOKED -> return next == RegionalSwapNoticeStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RegionalSwapNoticeStatus) {
        if (!canMoveTo(next)) {
            error("regional swap notice $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedTotal(): Double = round(amount * startsAt.toDouble() * 100) / 100

    fun snapshot(): RegionalSwapNotice {
        return RegionalSwapNotice(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            startsAt = startsAt,
            ownerId = ownerId,
            margin = margin,
            attributes = attributes,
            amount = amount,
            notes = notes,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional swap notice in status requested. */
fun newRegionalSwapNotice(code: String, label: String): RegionalSwapNotice {
    return RegionalSwapNotice(
        code = code,
        label = label,
        fuelLiters = 0.0,
        startsAt = 0,
        ownerId = "",
        margin = 0.0,
        attributes = mutableMapOf<String, String>(),
        amount = 0.0,
        notes = "",
        attempts = 0,
        status = RegionalSwapNoticeStatus.REQUESTED,
    )
}
