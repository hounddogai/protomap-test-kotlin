package com.acme.transport.emissions.carbonreport.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback carbon report record is in its life cycle. */
enum class FallbackCarbonReportRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a fallback carbon report record and its current status. */
data class FallbackCarbonReportRecord(
    var code: String,
    var label: String,
    var margin: Double,
    var updatedAt: Int,
    var startsAt: Int,
    var dock: String,
    var sequence: Int,
    var status: FallbackCarbonReportRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCarbonReportRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCarbonReportRecordStatus): Boolean {
        when (status) {
            FallbackCarbonReportRecordStatus.NEW -> {
                when (next) {
                    FallbackCarbonReportRecordStatus.QUEUED -> return true
                }
                return false
            }
            FallbackCarbonReportRecordStatus.QUEUED -> {
                when (next) {
                    FallbackCarbonReportRecordStatus.RUNNING, FallbackCarbonReportRecordStatus.FAILED -> return true
                }
                return false
            }
            FallbackCarbonReportRecordStatus.RUNNING -> return next == FallbackCarbonReportRecordStatus.DONE || next == FallbackCarbonReportRecordStatus.FAILED
            FallbackCarbonReportRecordStatus.DONE -> return next == FallbackCarbonReportRecordStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FallbackCarbonReportRecordStatus) {
        if (!canMoveTo(next)) {
            error("fallback carbon report record $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"fallback carbon report record"} $code"

    fun adjustedExposure(): Double = round((startsAt.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): FallbackCarbonReportRecord {
        return FallbackCarbonReportRecord(
            code = code,
            label = label,
            margin = margin,
            updatedAt = updatedAt,
            startsAt = startsAt,
            dock = dock,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback carbon report record in status new. */
fun newFallbackCarbonReportRecord(code: String, label: String): FallbackCarbonReportRecord {
    return FallbackCarbonReportRecord(
        code = code,
        label = label,
        margin = 0.0,
        updatedAt = 0,
        startsAt = 0,
        dock = "",
        sequence = 0,
        status = FallbackCarbonReportRecordStatus.NEW,
    )
}
