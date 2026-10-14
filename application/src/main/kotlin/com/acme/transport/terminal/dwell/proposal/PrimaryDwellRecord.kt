package com.acme.transport.terminal.dwell.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary dwell record is in its life cycle. */
enum class PrimaryDwellRecordStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a primary dwell record and its current status. */
data class PrimaryDwellRecord(
    var code: String,
    var name: String,
    var revision: Int,
    var capacity: Int,
    var threshold: Double,
    var status: PrimaryDwellRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDwellRecordStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDwellRecordStatus): Boolean {
        when (status) {
            PrimaryDwellRecordStatus.DRAFT -> {
                when (next) {
                    PrimaryDwellRecordStatus.SUSPENDED, PrimaryDwellRecordStatus.CLOSED -> return true
                }
                return false
            }
            PrimaryDwellRecordStatus.PENDING -> {
                when (next) {
                    PrimaryDwellRecordStatus.ACTIVE, PrimaryDwellRecordStatus.CLOSED -> return true
                }
                return false
            }
            PrimaryDwellRecordStatus.ACTIVE -> {
                when (next) {
                    PrimaryDwellRecordStatus.SUSPENDED, PrimaryDwellRecordStatus.CLOSED -> return true
                }
                return false
            }
            PrimaryDwellRecordStatus.SUSPENDED -> {
                when (next) {
                    PrimaryDwellRecordStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryDwellRecordStatus) {
        if (!canMoveTo(next)) {
            error("primary dwell record $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun effectiveVolume(): Double = round((revision.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): PrimaryDwellRecord {
        return PrimaryDwellRecord(
            code = code,
            name = name,
            revision = revision,
            capacity = capacity,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary dwell record in status draft. */
fun newPrimaryDwellRecord(code: String, name: String): PrimaryDwellRecord {
    return PrimaryDwellRecord(
        code = code,
        name = name,
        revision = 0,
        capacity = 0,
        threshold = 0.0,
        status = PrimaryDwellRecordStatus.DRAFT,
    )
}
