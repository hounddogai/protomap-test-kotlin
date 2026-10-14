package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback rate card entry is in its life cycle. */
enum class FallbackRateCardEntryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One fallback rate card entry, with what the planners need to act on it. */
data class FallbackRateCardEntry(
    var code: String,
    var label: String,
    var locale: String,
    var segment: String,
    var archived: Boolean,
    var retries: Int,
    var pinned: Boolean,
    var carrier: String,
    var floorValue: Double,
    var shiftCode: String,
    var country: String,
    var status: FallbackRateCardEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRateCardEntryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRateCardEntryStatus): Boolean {
        when (status) {
            FallbackRateCardEntryStatus.DRAFT -> {
                when (next) {
                    FallbackRateCardEntryStatus.PENDING -> return true
                }
                return false
            }
            FallbackRateCardEntryStatus.PENDING -> return next == FallbackRateCardEntryStatus.SUSPENDED
            FallbackRateCardEntryStatus.ACTIVE -> return next == FallbackRateCardEntryStatus.SUSPENDED || next == FallbackRateCardEntryStatus.CLOSED
            FallbackRateCardEntryStatus.SUSPENDED -> {
                when (next) {
                    FallbackRateCardEntryStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackRateCardEntryStatus) {
        if (!canMoveTo(next)) {
            error("fallback rate card entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveTotal(): Double = round((retries.toDouble() - floorValue) * 100) / 100

    fun snapshot(): FallbackRateCardEntry {
        return FallbackRateCardEntry(
            code = code,
            label = label,
            locale = locale,
            segment = segment,
            archived = archived,
            retries = retries,
            pinned = pinned,
            carrier = carrier,
            floorValue = floorValue,
            shiftCode = shiftCode,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback rate card entry in status draft. */
fun newFallbackRateCardEntry(code: String, label: String): FallbackRateCardEntry {
    return FallbackRateCardEntry(
        code = code,
        label = label,
        locale = "",
        segment = "",
        archived = false,
        retries = 0,
        pinned = false,
        carrier = "",
        floorValue = 0.0,
        shiftCode = "",
        country = "",
        status = FallbackRateCardEntryStatus.DRAFT,
    )
}
