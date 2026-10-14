package com.acme.transport.terminal.traileryard.recorditem

import com.acme.transport.kit.text.normalize

// The fallback trailer yard record records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a fallback trailer yard record is in its life cycle. */
enum class FallbackTrailerYardRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a fallback trailer yard record and its current status. */
data class FallbackTrailerYardRecord(
    var code: String,
    var label: String,
    var sequence: Int,
    var externalRef: String,
    var ownerId: String,
    var status: FallbackTrailerYardRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackTrailerYardRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackTrailerYardRecordStatus): Boolean {
        when (status) {
            FallbackTrailerYardRecordStatus.REQUESTED -> return next == FallbackTrailerYardRecordStatus.GRANTED || next == FallbackTrailerYardRecordStatus.REVOKED
            FallbackTrailerYardRecordStatus.GRANTED -> {
                when (next) {
                    FallbackTrailerYardRecordStatus.REVOKED, FallbackTrailerYardRecordStatus.EXPIRED -> return true
                }
                return false
            }
            FallbackTrailerYardRecordStatus.REVOKED -> {
                when (next) {
                    FallbackTrailerYardRecordStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackTrailerYardRecordStatus) {
        if (!canMoveTo(next)) {
            error("fallback trailer yard record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): FallbackTrailerYardRecord {
        return FallbackTrailerYardRecord(
            code = code,
            label = label,
            sequence = sequence,
            externalRef = externalRef,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback trailer yard record in status requested. */
fun newFallbackTrailerYardRecord(code: String, label: String): FallbackTrailerYardRecord {
    return FallbackTrailerYardRecord(
        code = code,
        label = label,
        sequence = 0,
        externalRef = "",
        ownerId = "",
        status = FallbackTrailerYardRecordStatus.REQUESTED,
    )
}
