package com.acme.transport.freightpay.accessorial.accessorialbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive accessorial slot is in its life cycle. */
enum class AdaptiveAccessorialSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A adaptive accessorial slot. Its code is unique within a tenant. */
data class AdaptiveAccessorialSlot(
    var code: String,
    var name: String,
    var amount: Double,
    var sequence: Int,
    var capacity: Int,
    var source: String,
    var dock: String,
    var region: String,
    var threshold: Double,
    var status: AdaptiveAccessorialSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAccessorialSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAccessorialSlotStatus): Boolean {
        when (status) {
            AdaptiveAccessorialSlotStatus.OPEN -> return next == AdaptiveAccessorialSlotStatus.IN_REVIEW || next == AdaptiveAccessorialSlotStatus.RESOLVED
            AdaptiveAccessorialSlotStatus.IN_REVIEW -> return next == AdaptiveAccessorialSlotStatus.RESOLVED || next == AdaptiveAccessorialSlotStatus.REOPENED
            AdaptiveAccessorialSlotStatus.RESOLVED -> {
                when (next) {
                    AdaptiveAccessorialSlotStatus.REOPENED, AdaptiveAccessorialSlotStatus.ARCHIVED -> return true
                }
                return false
            }
            AdaptiveAccessorialSlotStatus.REOPENED -> return next == AdaptiveAccessorialSlotStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AdaptiveAccessorialSlotStatus) {
        if (!canMoveTo(next)) {
            error("adaptive accessorial slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netExposure(): Double = round((sequence.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveAccessorialSlot {
        return AdaptiveAccessorialSlot(
            code = code,
            name = name,
            amount = amount,
            sequence = sequence,
            capacity = capacity,
            source = source,
            dock = dock,
            region = region,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a adaptive accessorial slot in status open. */
fun newAdaptiveAccessorialSlot(code: String, name: String): AdaptiveAccessorialSlot {
    return AdaptiveAccessorialSlot(
        code = code,
        name = name,
        amount = 0.0,
        sequence = 0,
        capacity = 0,
        source = "",
        dock = "",
        region = "",
        threshold = 0.0,
        status = AdaptiveAccessorialSlotStatus.OPEN,
    )
}
