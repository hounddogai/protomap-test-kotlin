package com.acme.transport.customs.hold.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hold record records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a hold record is in its life cycle. */
enum class HoldRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a hold record and its current status. */
data class HoldRecord(
    var code: String,
    var name: String,
    var capacity: Int,
    var checksum: String,
    var summary: String,
    var updatedAt: Int,
    var reviewer: String,
    var status: HoldRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldRecordStatus): Boolean {
        when (status) {
            HoldRecordStatus.PROPOSED -> return next == HoldRecordStatus.REJECTED || next == HoldRecordStatus.WITHDRAWN
            HoldRecordStatus.APPROVED -> return next == HoldRecordStatus.REJECTED
            HoldRecordStatus.REJECTED -> return next == HoldRecordStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: HoldRecordStatus) {
        if (!canMoveTo(next)) {
            error("hold record $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"hold record"} $code"

    fun effectiveTotal(): Double = round((capacity.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): HoldRecord {
        return HoldRecord(
            code = code,
            name = name,
            capacity = capacity,
            checksum = checksum,
            summary = summary,
            updatedAt = updatedAt,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a hold record in status proposed. */
fun newHoldRecord(code: String, name: String): HoldRecord {
    return HoldRecord(
        code = code,
        name = name,
        capacity = 0,
        checksum = "",
        summary = "",
        updatedAt = 0,
        reviewer = "",
        status = HoldRecordStatus.PROPOSED,
    )
}
