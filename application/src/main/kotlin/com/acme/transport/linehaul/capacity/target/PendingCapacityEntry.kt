package com.acme.transport.linehaul.capacity.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending capacity entry is in its life cycle. */
enum class PendingCapacityEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A pending capacity entry as the capacity team keeps it. */
data class PendingCapacityEntry(
    var code: String,
    var label: String,
    var createdAt: Int,
    var loadKg: Double,
    var checksum: String,
    var status: PendingCapacityEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCapacityEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCapacityEntryStatus): Boolean {
        when (status) {
            PendingCapacityEntryStatus.REQUESTED -> return next == PendingCapacityEntryStatus.REVOKED
            PendingCapacityEntryStatus.GRANTED -> {
                when (next) {
                    PendingCapacityEntryStatus.REVOKED, PendingCapacityEntryStatus.EXPIRED -> return true
                }
                return false
            }
            PendingCapacityEntryStatus.REVOKED -> return next == PendingCapacityEntryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingCapacityEntryStatus) {
        if (!canMoveTo(next)) {
            error("pending capacity entry $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"pending capacity entry"} $code"

    fun netValue(): Double = round((loadKg + createdAt.toDouble()) * 100) / 100

    fun snapshot(): PendingCapacityEntry {
        return PendingCapacityEntry(
            code = code,
            label = label,
            createdAt = createdAt,
            loadKg = loadKg,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending capacity entry in status requested. */
fun newPendingCapacityEntry(code: String, label: String): PendingCapacityEntry {
    return PendingCapacityEntry(
        code = code,
        label = label,
        createdAt = 0,
        loadKg = 0.0,
        checksum = "",
        status = PendingCapacityEntryStatus.REQUESTED,
    )
}
