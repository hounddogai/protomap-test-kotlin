package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender record is in its life cycle. */
enum class TenderRecordStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a tender record and its current status. */
data class TenderRecord(
    var code: String,
    var title: String,
    var country: String,
    var budget: Double,
    var segment: String,
    var stopCount: Int,
    var status: TenderRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderRecordStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderRecordStatus): Boolean {
        when (status) {
            TenderRecordStatus.PLANNED -> return next == TenderRecordStatus.COMPLETED || next == TenderRecordStatus.CANCELLED
            TenderRecordStatus.SCHEDULED -> {
                when (next) {
                    TenderRecordStatus.IN_PROGRESS, TenderRecordStatus.CANCELLED -> return true
                }
                return false
            }
            TenderRecordStatus.IN_PROGRESS -> {
                when (next) {
                    TenderRecordStatus.CANCELLED -> return true
                }
                return false
            }
            TenderRecordStatus.COMPLETED -> return next == TenderRecordStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TenderRecordStatus) {
        if (!canMoveTo(next)) {
            error("tender record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun netTotal(): Double = round(budget * stopCount.toDouble() * 100) / 100

    fun snapshot(): TenderRecord {
        return TenderRecord(
            code = code,
            title = title,
            country = country,
            budget = budget,
            segment = segment,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tender record in status planned. */
fun newTenderRecord(code: String, title: String): TenderRecord {
    return TenderRecord(
        code = code,
        title = title,
        country = "",
        budget = 0.0,
        segment = "",
        stopCount = 0,
        status = TenderRecordStatus.PLANNED,
    )
}
