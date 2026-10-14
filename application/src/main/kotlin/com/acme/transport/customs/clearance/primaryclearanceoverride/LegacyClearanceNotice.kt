package com.acme.transport.customs.clearance.primaryclearanceoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy clearance notice is in its life cycle. */
enum class LegacyClearanceNoticeStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a legacy clearance notice and its current status. */
data class LegacyClearanceNotice(
    var code: String,
    var label: String,
    var title: String,
    var expiresAt: Int,
    var notes: String,
    var batchSize: Int,
    var amount: Double,
    var durationMinutes: Int,
    var status: LegacyClearanceNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyClearanceNoticeStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyClearanceNoticeStatus): Boolean {
        when (status) {
            LegacyClearanceNoticeStatus.CREATED -> return next == LegacyClearanceNoticeStatus.VALIDATED || next == LegacyClearanceNoticeStatus.PUBLISHED
            LegacyClearanceNoticeStatus.VALIDATED -> {
                when (next) {
                    LegacyClearanceNoticeStatus.RETIRED -> return true
                }
                return false
            }
            LegacyClearanceNoticeStatus.PUBLISHED -> {
                when (next) {
                    LegacyClearanceNoticeStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyClearanceNoticeStatus) {
        if (!canMoveTo(next)) {
            error("legacy clearance notice $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"legacy clearance notice"} $code"

    fun netTotal(): Double = round((expiresAt.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): LegacyClearanceNotice {
        return LegacyClearanceNotice(
            code = code,
            label = label,
            title = title,
            expiresAt = expiresAt,
            notes = notes,
            batchSize = batchSize,
            amount = amount,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy clearance notice in status created. */
fun newLegacyClearanceNotice(code: String, label: String): LegacyClearanceNotice {
    return LegacyClearanceNotice(
        code = code,
        label = label,
        title = "",
        expiresAt = 0,
        notes = "",
        batchSize = 0,
        amount = 0.0,
        durationMinutes = 0,
        status = LegacyClearanceNoticeStatus.CREATED,
    )
}
