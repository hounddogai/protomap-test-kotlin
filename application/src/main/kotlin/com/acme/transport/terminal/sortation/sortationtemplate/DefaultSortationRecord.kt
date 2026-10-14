package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default sortation record is in its life cycle. */
enum class DefaultSortationRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a default sortation record and its current status. */
data class DefaultSortationRecord(
    var code: String,
    var label: String,
    var capacity: Int,
    var revision: Int,
    var batchSize: Int,
    var amount: Double,
    var status: DefaultSortationRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultSortationRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultSortationRecordStatus): Boolean {
        when (status) {
            DefaultSortationRecordStatus.REQUESTED -> {
                when (next) {
                    DefaultSortationRecordStatus.REVOKED, DefaultSortationRecordStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultSortationRecordStatus.GRANTED -> return next == DefaultSortationRecordStatus.REVOKED || next == DefaultSortationRecordStatus.EXPIRED
            DefaultSortationRecordStatus.REVOKED -> {
                when (next) {
                    DefaultSortationRecordStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultSortationRecordStatus) {
        if (!canMoveTo(next)) {
            error("default sortation record $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun netLoad(): Double = round((revision.toDouble() + amount) * 100) / 100

    fun snapshot(): DefaultSortationRecord {
        return DefaultSortationRecord(
            code = code,
            label = label,
            capacity = capacity,
            revision = revision,
            batchSize = batchSize,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default sortation record in status requested. */
fun newDefaultSortationRecord(code: String, label: String): DefaultSortationRecord {
    return DefaultSortationRecord(
        code = code,
        label = label,
        capacity = 0,
        revision = 0,
        batchSize = 0,
        amount = 0.0,
        status = DefaultSortationRecordStatus.REQUESTED,
    )
}
