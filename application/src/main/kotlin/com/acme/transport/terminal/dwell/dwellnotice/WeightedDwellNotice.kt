package com.acme.transport.terminal.dwell.dwellnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted dwell notice records of terminal's dwell work, and the rules, stores, and calculations around
// them.

/** Where a weighted dwell notice is in its life cycle. */
enum class WeightedDwellNoticeStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A weighted dwell notice. Its code is unique within a tenant. */
data class WeightedDwellNotice(
    var code: String,
    var title: String,
    var pallets: Int,
    var capacity: Int,
    var shiftCode: String,
    var pinned: Boolean,
    var priority: Int,
    var externalRef: String,
    var label: String,
    var margin: Double,
    var status: WeightedDwellNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDwellNoticeStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDwellNoticeStatus): Boolean {
        when (status) {
            WeightedDwellNoticeStatus.REQUESTED -> return next == WeightedDwellNoticeStatus.REVOKED
            WeightedDwellNoticeStatus.GRANTED -> return next == WeightedDwellNoticeStatus.EXPIRED
            WeightedDwellNoticeStatus.REVOKED -> return next == WeightedDwellNoticeStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: WeightedDwellNoticeStatus) {
        if (!canMoveTo(next)) {
            error("weighted dwell notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $title"

    fun adjustedLoad(): Double = round(margin * pallets.toDouble() * 100) / 100

    fun snapshot(): WeightedDwellNotice {
        return WeightedDwellNotice(
            code = code,
            title = title,
            pallets = pallets,
            capacity = capacity,
            shiftCode = shiftCode,
            pinned = pinned,
            priority = priority,
            externalRef = externalRef,
            label = label,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted dwell notice in status requested. */
fun newWeightedDwellNotice(code: String, title: String): WeightedDwellNotice {
    return WeightedDwellNotice(
        code = code,
        title = title,
        pallets = 0,
        capacity = 0,
        shiftCode = "",
        pinned = false,
        priority = 0,
        externalRef = "",
        label = "",
        margin = 0.0,
        status = WeightedDwellNoticeStatus.REQUESTED,
    )
}
