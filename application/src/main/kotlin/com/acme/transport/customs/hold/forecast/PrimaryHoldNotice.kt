package com.acme.transport.customs.hold.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary hold notice is in its life cycle. */
enum class PrimaryHoldNoticeStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a primary hold notice and its current status. */
data class PrimaryHoldNotice(
    var code: String,
    var name: String,
    var source: String,
    var pallets: Int,
    var batchSize: Int,
    var currency: String,
    var floorValue: Double,
    var status: PrimaryHoldNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryHoldNoticeStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryHoldNoticeStatus): Boolean {
        when (status) {
            PrimaryHoldNoticeStatus.PLANNED -> {
                when (next) {
                    PrimaryHoldNoticeStatus.SCHEDULED, PrimaryHoldNoticeStatus.COMPLETED -> return true
                }
                return false
            }
            PrimaryHoldNoticeStatus.SCHEDULED -> {
                when (next) {
                    PrimaryHoldNoticeStatus.IN_PROGRESS, PrimaryHoldNoticeStatus.CANCELLED -> return true
                }
                return false
            }
            PrimaryHoldNoticeStatus.IN_PROGRESS -> return next == PrimaryHoldNoticeStatus.COMPLETED
            PrimaryHoldNoticeStatus.COMPLETED -> return next == PrimaryHoldNoticeStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PrimaryHoldNoticeStatus) {
        if (!canMoveTo(next)) {
            error("primary hold notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveExposure(): Double = round((batchSize.toDouble() + floorValue) * 100) / 100

    fun snapshot(): PrimaryHoldNotice {
        return PrimaryHoldNotice(
            code = code,
            name = name,
            source = source,
            pallets = pallets,
            batchSize = batchSize,
            currency = currency,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a primary hold notice in status planned. */
fun newPrimaryHoldNotice(code: String, name: String): PrimaryHoldNotice {
    return PrimaryHoldNotice(
        code = code,
        name = name,
        source = "",
        pallets = 0,
        batchSize = 0,
        currency = "",
        floorValue = 0.0,
        status = PrimaryHoldNoticeStatus.PLANNED,
    )
}
