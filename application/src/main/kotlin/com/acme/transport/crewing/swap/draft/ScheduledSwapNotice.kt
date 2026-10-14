package com.acme.transport.crewing.swap.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled swap notice is in its life cycle. */
enum class ScheduledSwapNoticeStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled swap notice as the swap team keeps it. */
data class ScheduledSwapNotice(
    var code: String,
    var name: String,
    var carrier: String,
    var durationMinutes: Int,
    var floorValue: Double,
    var amount: Double,
    var quantity: Int,
    var maximum: Double,
    var contactEmail: String,
    var label: String,
    var status: ScheduledSwapNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledSwapNoticeStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledSwapNoticeStatus): Boolean {
        when (status) {
            ScheduledSwapNoticeStatus.DRAFT -> {
                when (next) {
                    ScheduledSwapNoticeStatus.ACTIVE -> return true
                }
                return false
            }
            ScheduledSwapNoticeStatus.PENDING -> {
                when (next) {
                    ScheduledSwapNoticeStatus.ACTIVE, ScheduledSwapNoticeStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledSwapNoticeStatus.ACTIVE -> {
                when (next) {
                    ScheduledSwapNoticeStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledSwapNoticeStatus.SUSPENDED -> return next == ScheduledSwapNoticeStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScheduledSwapNoticeStatus) {
        if (!canMoveTo(next)) {
            error("scheduled swap notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $name"

    fun adjustedTotal(): Double = round((floorValue - maximum) * 100) / 100

    fun snapshot(): ScheduledSwapNotice {
        return ScheduledSwapNotice(
            code = code,
            name = name,
            carrier = carrier,
            durationMinutes = durationMinutes,
            floorValue = floorValue,
            amount = amount,
            quantity = quantity,
            maximum = maximum,
            contactEmail = contactEmail,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled swap notice in status draft. */
fun newScheduledSwapNotice(code: String, name: String): ScheduledSwapNotice {
    return ScheduledSwapNotice(
        code = code,
        name = name,
        carrier = "",
        durationMinutes = 0,
        floorValue = 0.0,
        amount = 0.0,
        quantity = 0,
        maximum = 0.0,
        contactEmail = "",
        label = "",
        status = ScheduledSwapNoticeStatus.DRAFT,
    )
}
