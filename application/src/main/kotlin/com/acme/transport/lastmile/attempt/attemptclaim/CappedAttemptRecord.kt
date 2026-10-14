package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped attempt record is in its life cycle. */
enum class CappedAttemptRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a capped attempt record and its current status. */
data class CappedAttemptRecord(
    var code: String,
    var name: String,
    var visible: Boolean,
    var lane: String,
    var sequence: Int,
    var active: Boolean,
    var quantity: Int,
    var revision: Int,
    var status: CappedAttemptRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedAttemptRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedAttemptRecordStatus): Boolean {
        when (status) {
            CappedAttemptRecordStatus.CREATED -> {
                when (next) {
                    CappedAttemptRecordStatus.PUBLISHED -> return true
                }
                return false
            }
            CappedAttemptRecordStatus.VALIDATED -> return next == CappedAttemptRecordStatus.PUBLISHED || next == CappedAttemptRecordStatus.RETIRED
            CappedAttemptRecordStatus.PUBLISHED -> {
                when (next) {
                    CappedAttemptRecordStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedAttemptRecordStatus) {
        if (!canMoveTo(next)) {
            error("capped attempt record $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun netValue(): Double = round(quantity.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): CappedAttemptRecord {
        return CappedAttemptRecord(
            code = code,
            name = name,
            visible = visible,
            lane = lane,
            sequence = sequence,
            active = active,
            quantity = quantity,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a capped attempt record in status created. */
fun newCappedAttemptRecord(code: String, name: String): CappedAttemptRecord {
    return CappedAttemptRecord(
        code = code,
        name = name,
        visible = false,
        lane = "",
        sequence = 0,
        active = false,
        quantity = 0,
        revision = 0,
        status = CappedAttemptRecordStatus.CREATED,
    )
}
