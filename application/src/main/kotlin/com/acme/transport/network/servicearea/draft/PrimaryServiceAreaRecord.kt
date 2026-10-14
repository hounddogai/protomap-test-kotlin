package com.acme.transport.network.servicearea.draft

import com.acme.transport.kit.text.normalize

/** Where a primary service area record is in its life cycle. */
enum class PrimaryServiceAreaRecordStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One primary service area record, with what the planners need to act on it. */
data class PrimaryServiceAreaRecord(
    var code: String,
    var label: String,
    var summary: String,
    var reviewer: String,
    var sequence: Int,
    var status: PrimaryServiceAreaRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryServiceAreaRecordStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryServiceAreaRecordStatus): Boolean {
        when (status) {
            PrimaryServiceAreaRecordStatus.PLANNED -> {
                when (next) {
                    PrimaryServiceAreaRecordStatus.SCHEDULED, PrimaryServiceAreaRecordStatus.CANCELLED -> return true
                }
                return false
            }
            PrimaryServiceAreaRecordStatus.SCHEDULED -> return next == PrimaryServiceAreaRecordStatus.COMPLETED
            PrimaryServiceAreaRecordStatus.IN_PROGRESS -> {
                when (next) {
                    PrimaryServiceAreaRecordStatus.CANCELLED -> return true
                }
                return false
            }
            PrimaryServiceAreaRecordStatus.COMPLETED -> return next == PrimaryServiceAreaRecordStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PrimaryServiceAreaRecordStatus) {
        if (!canMoveTo(next)) {
            error("primary service area record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): PrimaryServiceAreaRecord {
        return PrimaryServiceAreaRecord(
            code = code,
            label = label,
            summary = summary,
            reviewer = reviewer,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary service area record in status planned. */
fun newPrimaryServiceAreaRecord(code: String, label: String): PrimaryServiceAreaRecord {
    return PrimaryServiceAreaRecord(
        code = code,
        label = label,
        summary = "",
        reviewer = "",
        sequence = 0,
        status = PrimaryServiceAreaRecordStatus.PLANNED,
    )
}
