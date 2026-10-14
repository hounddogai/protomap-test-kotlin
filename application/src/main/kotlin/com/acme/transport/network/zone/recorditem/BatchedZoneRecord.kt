package com.acme.transport.network.zone.recorditem

import com.acme.transport.kit.text.normalize

/** Where a batched zone record is in its life cycle. */
enum class BatchedZoneRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a batched zone record and its current status. */
data class BatchedZoneRecord(
    var code: String,
    var label: String,
    var routeCode: String,
    var attempts: Int,
    var externalRef: String,
    var carrier: String,
    var lane: String,
    var segment: String,
    var status: BatchedZoneRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedZoneRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedZoneRecordStatus): Boolean {
        when (status) {
            BatchedZoneRecordStatus.REQUESTED -> {
                when (next) {
                    BatchedZoneRecordStatus.GRANTED, BatchedZoneRecordStatus.REVOKED -> return true
                }
                return false
            }
            BatchedZoneRecordStatus.GRANTED -> return next == BatchedZoneRecordStatus.REVOKED
            BatchedZoneRecordStatus.REVOKED -> return next == BatchedZoneRecordStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BatchedZoneRecordStatus) {
        if (!canMoveTo(next)) {
            error("batched zone record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): BatchedZoneRecord {
        return BatchedZoneRecord(
            code = code,
            label = label,
            routeCode = routeCode,
            attempts = attempts,
            externalRef = externalRef,
            carrier = carrier,
            lane = lane,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a batched zone record in status requested. */
fun newBatchedZoneRecord(code: String, label: String): BatchedZoneRecord {
    return BatchedZoneRecord(
        code = code,
        label = label,
        routeCode = "",
        attempts = 0,
        externalRef = "",
        carrier = "",
        lane = "",
        segment = "",
        status = BatchedZoneRecordStatus.REQUESTED,
    )
}
