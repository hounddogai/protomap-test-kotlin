package com.acme.transport.fleetops.defect.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect exception is in its life cycle. */
enum class DefectExceptionStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A defect exception. Its code is unique within a tenant. */
data class DefectException(
    var code: String,
    var name: String,
    var unit: String,
    var startsAt: Int,
    var segment: String,
    var driverId: String,
    var pallets: Int,
    var region: String,
    var source: String,
    var approved: Boolean,
    var limit: Int,
    var status: DefectExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectExceptionStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectExceptionStatus): Boolean {
        when (status) {
            DefectExceptionStatus.PLANNED -> return next == DefectExceptionStatus.SCHEDULED || next == DefectExceptionStatus.COMPLETED
            DefectExceptionStatus.SCHEDULED -> return next == DefectExceptionStatus.COMPLETED || next == DefectExceptionStatus.CANCELLED
            DefectExceptionStatus.IN_PROGRESS -> {
                when (next) {
                    DefectExceptionStatus.CANCELLED -> return true
                }
                return false
            }
            DefectExceptionStatus.COMPLETED -> {
                when (next) {
                    DefectExceptionStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectExceptionStatus) {
        if (!canMoveTo(next)) {
            error("defect exception $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"defect exception"} $code"

    fun projectedLoad(): Double = round((limit.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): DefectException {
        return DefectException(
            code = code,
            name = name,
            unit = unit,
            startsAt = startsAt,
            segment = segment,
            driverId = driverId,
            pallets = pallets,
            region = region,
            source = source,
            approved = approved,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a defect exception in status planned. */
fun newDefectException(code: String, name: String): DefectException {
    return DefectException(
        code = code,
        name = name,
        unit = "",
        startsAt = 0,
        segment = "",
        driverId = "",
        pallets = 0,
        region = "",
        source = "",
        approved = false,
        limit = 0,
        status = DefectExceptionStatus.PLANNED,
    )
}
