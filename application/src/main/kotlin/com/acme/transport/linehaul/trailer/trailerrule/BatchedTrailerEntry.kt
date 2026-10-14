package com.acme.transport.linehaul.trailer.trailerrule

import com.acme.transport.kit.text.normalize

/** Where a batched trailer entry is in its life cycle. */
enum class BatchedTrailerEntryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched trailer entry. Its code is unique within a tenant. */
data class BatchedTrailerEntry(
    var code: String,
    var label: String,
    var checksum: String,
    var country: String,
    var distanceKm: Double,
    var status: BatchedTrailerEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTrailerEntryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTrailerEntryStatus): Boolean {
        when (status) {
            BatchedTrailerEntryStatus.IDLE -> return next == BatchedTrailerEntryStatus.READY || next == BatchedTrailerEntryStatus.STOPPED
            BatchedTrailerEntryStatus.WARMING -> return next == BatchedTrailerEntryStatus.READY
            BatchedTrailerEntryStatus.READY -> return next == BatchedTrailerEntryStatus.DRAINING
            BatchedTrailerEntryStatus.DRAINING -> return next == BatchedTrailerEntryStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BatchedTrailerEntryStatus) {
        if (!canMoveTo(next)) {
            error("batched trailer entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = distanceKm

    fun snapshot(): BatchedTrailerEntry {
        return BatchedTrailerEntry(
            code = code,
            label = label,
            checksum = checksum,
            country = country,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a batched trailer entry in status idle. */
fun newBatchedTrailerEntry(code: String, label: String): BatchedTrailerEntry {
    return BatchedTrailerEntry(
        code = code,
        label = label,
        checksum = "",
        country = "",
        distanceKm = 0.0,
        status = BatchedTrailerEntryStatus.IDLE,
    )
}
