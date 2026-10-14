package com.acme.transport.terminal.traileryard.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted trailer yard entry records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a weighted trailer yard entry is in its life cycle. */
enum class WeightedTrailerYardEntryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted trailer yard entry. Its code is unique within a tenant. */
data class WeightedTrailerYardEntry(
    var code: String,
    var label: String,
    var ownerId: String,
    var currency: String,
    var segment: String,
    var discount: Double,
    var phone: String,
    var ratio: Double,
    var country: String,
    var status: WeightedTrailerYardEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTrailerYardEntryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTrailerYardEntryStatus): Boolean {
        when (status) {
            WeightedTrailerYardEntryStatus.PLANNED -> return next == WeightedTrailerYardEntryStatus.CANCELLED
            WeightedTrailerYardEntryStatus.SCHEDULED -> {
                when (next) {
                    WeightedTrailerYardEntryStatus.COMPLETED, WeightedTrailerYardEntryStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedTrailerYardEntryStatus.IN_PROGRESS -> {
                when (next) {
                    WeightedTrailerYardEntryStatus.COMPLETED -> return true
                }
                return false
            }
            WeightedTrailerYardEntryStatus.COMPLETED -> {
                when (next) {
                    WeightedTrailerYardEntryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedTrailerYardEntryStatus) {
        if (!canMoveTo(next)) {
            error("weighted trailer yard entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedTotal(): Double = round((ratio - discount) * 100) / 100

    fun snapshot(): WeightedTrailerYardEntry {
        return WeightedTrailerYardEntry(
            code = code,
            label = label,
            ownerId = ownerId,
            currency = currency,
            segment = segment,
            discount = discount,
            phone = phone,
            ratio = ratio,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a weighted trailer yard entry in status planned. */
fun newWeightedTrailerYardEntry(code: String, label: String): WeightedTrailerYardEntry {
    return WeightedTrailerYardEntry(
        code = code,
        label = label,
        ownerId = "",
        currency = "",
        segment = "",
        discount = 0.0,
        phone = "",
        ratio = 0.0,
        country = "",
        status = WeightedTrailerYardEntryStatus.PLANNED,
    )
}
