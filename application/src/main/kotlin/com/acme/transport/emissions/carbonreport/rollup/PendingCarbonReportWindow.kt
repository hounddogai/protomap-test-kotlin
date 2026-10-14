package com.acme.transport.emissions.carbonreport.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending carbon report window is in its life cycle. */
enum class PendingCarbonReportWindowStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a pending carbon report window and its current status. */
data class PendingCarbonReportWindow(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var label: String,
    var checksum: String,
    var retries: Int,
    var shiftCode: String,
    var ownerId: String,
    var quantity: Int,
    var status: PendingCarbonReportWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCarbonReportWindowStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCarbonReportWindowStatus): Boolean {
        when (status) {
            PendingCarbonReportWindowStatus.NEW -> {
                when (next) {
                    PendingCarbonReportWindowStatus.DONE -> return true
                }
                return false
            }
            PendingCarbonReportWindowStatus.QUEUED -> {
                when (next) {
                    PendingCarbonReportWindowStatus.FAILED -> return true
                }
                return false
            }
            PendingCarbonReportWindowStatus.RUNNING -> return next == PendingCarbonReportWindowStatus.FAILED
            PendingCarbonReportWindowStatus.DONE -> return next == PendingCarbonReportWindowStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PendingCarbonReportWindowStatus) {
        if (!canMoveTo(next)) {
            error("pending carbon report window $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $name"

    fun weightedValue(): Double = round((quantity.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): PendingCarbonReportWindow {
        return PendingCarbonReportWindow(
            code = code,
            name = name,
            expiresAt = expiresAt,
            label = label,
            checksum = checksum,
            retries = retries,
            shiftCode = shiftCode,
            ownerId = ownerId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending carbon report window in status new. */
fun newPendingCarbonReportWindow(code: String, name: String): PendingCarbonReportWindow {
    return PendingCarbonReportWindow(
        code = code,
        name = name,
        expiresAt = 0,
        label = "",
        checksum = "",
        retries = 0,
        shiftCode = "",
        ownerId = "",
        quantity = 0,
        status = PendingCarbonReportWindowStatus.NEW,
    )
}
