package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending hold entry is in its life cycle. */
enum class PendingHoldEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A pending hold entry. Its code is unique within a tenant. */
data class PendingHoldEntry(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var contactEmail: String,
    var limit: Int,
    var sequence: Int,
    var phone: String,
    var quantity: Int,
    var ratio: Double,
    var status: PendingHoldEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingHoldEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingHoldEntryStatus): Boolean {
        when (status) {
            PendingHoldEntryStatus.CREATED -> return next == PendingHoldEntryStatus.PUBLISHED
            PendingHoldEntryStatus.VALIDATED -> {
                when (next) {
                    PendingHoldEntryStatus.PUBLISHED, PendingHoldEntryStatus.RETIRED -> return true
                }
                return false
            }
            PendingHoldEntryStatus.PUBLISHED -> {
                when (next) {
                    PendingHoldEntryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingHoldEntryStatus) {
        if (!canMoveTo(next)) {
            error("pending hold entry $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"pending hold entry"} $code"

    fun adjustedTotal(): Double = round(limit.toDouble() * ratio * 100) / 100

    fun snapshot(): PendingHoldEntry {
        return PendingHoldEntry(
            code = code,
            title = title,
            updatedAt = updatedAt,
            contactEmail = contactEmail,
            limit = limit,
            sequence = sequence,
            phone = phone,
            quantity = quantity,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending hold entry in status created. */
fun newPendingHoldEntry(code: String, title: String): PendingHoldEntry {
    return PendingHoldEntry(
        code = code,
        title = title,
        updatedAt = 0,
        contactEmail = "",
        limit = 0,
        sequence = 0,
        phone = "",
        quantity = 0,
        ratio = 0.0,
        status = PendingHoldEntryStatus.CREATED,
    )
}
