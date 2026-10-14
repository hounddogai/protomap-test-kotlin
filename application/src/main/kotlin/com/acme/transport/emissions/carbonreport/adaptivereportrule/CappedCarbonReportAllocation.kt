package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped carbon report allocation is in its life cycle. */
enum class CappedCarbonReportAllocationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a capped carbon report allocation and its current status. */
data class CappedCarbonReportAllocation(
    var code: String,
    var label: String,
    var odometer: Int,
    var startsAt: Int,
    var attempts: Int,
    var segment: String,
    var notes: String,
    var maximum: Double,
    var channel: String,
    var status: CappedCarbonReportAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCarbonReportAllocationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCarbonReportAllocationStatus): Boolean {
        when (status) {
            CappedCarbonReportAllocationStatus.REQUESTED -> {
                when (next) {
                    CappedCarbonReportAllocationStatus.GRANTED, CappedCarbonReportAllocationStatus.EXPIRED -> return true
                }
                return false
            }
            CappedCarbonReportAllocationStatus.GRANTED -> {
                when (next) {
                    CappedCarbonReportAllocationStatus.REVOKED, CappedCarbonReportAllocationStatus.EXPIRED -> return true
                }
                return false
            }
            CappedCarbonReportAllocationStatus.REVOKED -> {
                when (next) {
                    CappedCarbonReportAllocationStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedCarbonReportAllocationStatus) {
        if (!canMoveTo(next)) {
            error("capped carbon report allocation $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"capped carbon report allocation"} $code"

    fun adjustedExposure(): Double = round(startsAt.toDouble() * attempts.toDouble() * 100) / 100

    fun snapshot(): CappedCarbonReportAllocation {
        return CappedCarbonReportAllocation(
            code = code,
            label = label,
            odometer = odometer,
            startsAt = startsAt,
            attempts = attempts,
            segment = segment,
            notes = notes,
            maximum = maximum,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a capped carbon report allocation in status requested. */
fun newCappedCarbonReportAllocation(code: String, label: String): CappedCarbonReportAllocation {
    return CappedCarbonReportAllocation(
        code = code,
        label = label,
        odometer = 0,
        startsAt = 0,
        attempts = 0,
        segment = "",
        notes = "",
        maximum = 0.0,
        channel = "",
        status = CappedCarbonReportAllocationStatus.REQUESTED,
    )
}
