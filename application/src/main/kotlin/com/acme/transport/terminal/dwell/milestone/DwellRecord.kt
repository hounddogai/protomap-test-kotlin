package com.acme.transport.terminal.dwell.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell record is in its life cycle. */
enum class DwellRecordStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A dwell record. Its code is unique within a tenant. */
data class DwellRecord(
    var code: String,
    var title: String,
    var revision: Int,
    var phone: String,
    var vehicleId: String,
    var region: String,
    var budget: Double,
    var score: Double,
    var cost: Double,
    var discount: Double,
    var status: DwellRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellRecordStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellRecordStatus): Boolean {
        when (status) {
            DwellRecordStatus.PLANNED -> return next == DwellRecordStatus.SCHEDULED
            DwellRecordStatus.SCHEDULED -> {
                when (next) {
                    DwellRecordStatus.IN_PROGRESS, DwellRecordStatus.CANCELLED -> return true
                }
                return false
            }
            DwellRecordStatus.IN_PROGRESS -> return next == DwellRecordStatus.CANCELLED
            DwellRecordStatus.COMPLETED -> {
                when (next) {
                    DwellRecordStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellRecordStatus) {
        if (!canMoveTo(next)) {
            error("dwell record $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun effectiveValue(): Double = round((discount + budget) * 100) / 100

    fun snapshot(): DwellRecord {
        return DwellRecord(
            code = code,
            title = title,
            revision = revision,
            phone = phone,
            vehicleId = vehicleId,
            region = region,
            budget = budget,
            score = score,
            cost = cost,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a dwell record in status planned. */
fun newDwellRecord(code: String, title: String): DwellRecord {
    return DwellRecord(
        code = code,
        title = title,
        revision = 0,
        phone = "",
        vehicleId = "",
        region = "",
        budget = 0.0,
        score = 0.0,
        cost = 0.0,
        discount = 0.0,
        status = DwellRecordStatus.PLANNED,
    )
}
