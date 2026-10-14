package com.acme.transport.linehaul.capacity.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive capacity entry records of linehaul's capacity work, and the rules, stores, and calculations
// around them.

/** Where a adaptive capacity entry is in its life cycle. */
enum class AdaptiveCapacityEntryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A adaptive capacity entry. Its code is unique within a tenant. */
data class AdaptiveCapacityEntry(
    var code: String,
    var title: String,
    var minimum: Double,
    var contactEmail: String,
    var quantity: Int,
    var durationMinutes: Int,
    var active: Boolean,
    var amount: Double,
    var visible: Boolean,
    var phone: String,
    var limit: Int,
    var status: AdaptiveCapacityEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCapacityEntryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCapacityEntryStatus): Boolean {
        when (status) {
            AdaptiveCapacityEntryStatus.DRAFT -> {
                when (next) {
                    AdaptiveCapacityEntryStatus.ACTIVE, AdaptiveCapacityEntryStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveCapacityEntryStatus.PENDING -> return next == AdaptiveCapacityEntryStatus.ACTIVE || next == AdaptiveCapacityEntryStatus.SUSPENDED
            AdaptiveCapacityEntryStatus.ACTIVE -> return next == AdaptiveCapacityEntryStatus.SUSPENDED || next == AdaptiveCapacityEntryStatus.CLOSED
            AdaptiveCapacityEntryStatus.SUSPENDED -> return next == AdaptiveCapacityEntryStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AdaptiveCapacityEntryStatus) {
        if (!canMoveTo(next)) {
            error("adaptive capacity entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedVolume(): Double = round((durationMinutes.toDouble() + minimum) * 100) / 100

    fun snapshot(): AdaptiveCapacityEntry {
        return AdaptiveCapacityEntry(
            code = code,
            title = title,
            minimum = minimum,
            contactEmail = contactEmail,
            quantity = quantity,
            durationMinutes = durationMinutes,
            active = active,
            amount = amount,
            visible = visible,
            phone = phone,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive capacity entry in status draft. */
fun newAdaptiveCapacityEntry(code: String, title: String): AdaptiveCapacityEntry {
    return AdaptiveCapacityEntry(
        code = code,
        title = title,
        minimum = 0.0,
        contactEmail = "",
        quantity = 0,
        durationMinutes = 0,
        active = false,
        amount = 0.0,
        visible = false,
        phone = "",
        limit = 0,
        status = AdaptiveCapacityEntryStatus.DRAFT,
    )
}
