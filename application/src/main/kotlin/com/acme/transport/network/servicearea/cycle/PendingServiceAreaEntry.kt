package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending service area entry is in its life cycle. */
enum class PendingServiceAreaEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A pending service area entry. Its code is unique within a tenant. */
data class PendingServiceAreaEntry(
    var code: String,
    var title: String,
    var contactEmail: String,
    var margin: Double,
    var archived: Boolean,
    var fuelLiters: Double,
    var description: String,
    var budget: Double,
    var createdAt: Int,
    var status: PendingServiceAreaEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingServiceAreaEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingServiceAreaEntryStatus): Boolean {
        when (status) {
            PendingServiceAreaEntryStatus.REQUESTED -> {
                when (next) {
                    PendingServiceAreaEntryStatus.GRANTED, PendingServiceAreaEntryStatus.REVOKED -> return true
                }
                return false
            }
            PendingServiceAreaEntryStatus.GRANTED -> return next == PendingServiceAreaEntryStatus.EXPIRED
            PendingServiceAreaEntryStatus.REVOKED -> {
                when (next) {
                    PendingServiceAreaEntryStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingServiceAreaEntryStatus) {
        if (!canMoveTo(next)) {
            error("pending service area entry $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun netVolume(): Double = round((margin - createdAt.toDouble()) * 100) / 100

    fun snapshot(): PendingServiceAreaEntry {
        return PendingServiceAreaEntry(
            code = code,
            title = title,
            contactEmail = contactEmail,
            margin = margin,
            archived = archived,
            fuelLiters = fuelLiters,
            description = description,
            budget = budget,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending service area entry in status requested. */
fun newPendingServiceAreaEntry(code: String, title: String): PendingServiceAreaEntry {
    return PendingServiceAreaEntry(
        code = code,
        title = title,
        contactEmail = "",
        margin = 0.0,
        archived = false,
        fuelLiters = 0.0,
        description = "",
        budget = 0.0,
        createdAt = 0,
        status = PendingServiceAreaEntryStatus.REQUESTED,
    )
}
