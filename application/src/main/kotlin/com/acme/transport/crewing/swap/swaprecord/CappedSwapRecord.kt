package com.acme.transport.crewing.swap.swaprecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped swap record records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a capped swap record is in its life cycle. */
enum class CappedSwapRecordStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A capped swap record. Its code is unique within a tenant. */
data class CappedSwapRecord(
    var code: String,
    var name: String,
    var country: String,
    var driverId: String,
    var sequence: Int,
    var attempts: Int,
    var revision: Int,
    var weight: Double,
    var status: CappedSwapRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedSwapRecordStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedSwapRecordStatus): Boolean {
        when (status) {
            CappedSwapRecordStatus.OPEN -> {
                when (next) {
                    CappedSwapRecordStatus.RESOLVED, CappedSwapRecordStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedSwapRecordStatus.IN_REVIEW -> return next == CappedSwapRecordStatus.ARCHIVED
            CappedSwapRecordStatus.RESOLVED -> {
                when (next) {
                    CappedSwapRecordStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedSwapRecordStatus.REOPENED -> {
                when (next) {
                    CappedSwapRecordStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedSwapRecordStatus) {
        if (!canMoveTo(next)) {
            error("capped swap record $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"capped swap record"} $code"

    fun effectiveVolume(): Double = round(attempts.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): CappedSwapRecord {
        return CappedSwapRecord(
            code = code,
            name = name,
            country = country,
            driverId = driverId,
            sequence = sequence,
            attempts = attempts,
            revision = revision,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capped swap record in status open. */
fun newCappedSwapRecord(code: String, name: String): CappedSwapRecord {
    return CappedSwapRecord(
        code = code,
        name = name,
        country = "",
        driverId = "",
        sequence = 0,
        attempts = 0,
        revision = 0,
        weight = 0.0,
        status = CappedSwapRecordStatus.OPEN,
    )
}
