package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped rate card window is in its life cycle. */
enum class CappedRateCardWindowStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a capped rate card window and its current status. */
data class CappedRateCardWindow(
    var code: String,
    var title: String,
    var retries: Int,
    var timezone: String,
    var label: String,
    var reviewer: String,
    var capacity: Int,
    var version: Int,
    var pallets: Int,
    var weight: Double,
    var status: CappedRateCardWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRateCardWindowStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRateCardWindowStatus): Boolean {
        when (status) {
            CappedRateCardWindowStatus.OPEN -> {
                when (next) {
                    CappedRateCardWindowStatus.IN_REVIEW -> return true
                }
                return false
            }
            CappedRateCardWindowStatus.IN_REVIEW -> return next == CappedRateCardWindowStatus.RESOLVED || next == CappedRateCardWindowStatus.ARCHIVED
            CappedRateCardWindowStatus.RESOLVED -> {
                when (next) {
                    CappedRateCardWindowStatus.REOPENED, CappedRateCardWindowStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedRateCardWindowStatus.REOPENED -> return next == CappedRateCardWindowStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CappedRateCardWindowStatus) {
        if (!canMoveTo(next)) {
            error("capped rate card window $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code: $title"

    fun netTotal(): Double = round((retries.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): CappedRateCardWindow {
        return CappedRateCardWindow(
            code = code,
            title = title,
            retries = retries,
            timezone = timezone,
            label = label,
            reviewer = reviewer,
            capacity = capacity,
            version = version,
            pallets = pallets,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped rate card window in status open. */
fun newCappedRateCardWindow(code: String, title: String): CappedRateCardWindow {
    return CappedRateCardWindow(
        code = code,
        title = title,
        retries = 0,
        timezone = "",
        label = "",
        reviewer = "",
        capacity = 0,
        version = 0,
        pallets = 0,
        weight = 0.0,
        status = CappedRateCardWindowStatus.OPEN,
    )
}
