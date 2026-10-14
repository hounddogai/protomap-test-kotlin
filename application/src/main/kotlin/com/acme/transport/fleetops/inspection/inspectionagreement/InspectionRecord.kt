package com.acme.transport.fleetops.inspection.inspectionagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection record is in its life cycle. */
enum class InspectionRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A inspection record. Its code is unique within a tenant. */
data class InspectionRecord(
    var code: String,
    var name: String,
    var retries: Int,
    var description: String,
    var endsAt: Int,
    var maximum: Double,
    var status: InspectionRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionRecordStatus): Boolean {
        when (status) {
            InspectionRecordStatus.PROPOSED -> return next == InspectionRecordStatus.REJECTED
            InspectionRecordStatus.APPROVED -> {
                when (next) {
                    InspectionRecordStatus.REJECTED, InspectionRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
            InspectionRecordStatus.REJECTED -> return next == InspectionRecordStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: InspectionRecordStatus) {
        if (!canMoveTo(next)) {
            error("inspection record $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun netTotal(): Double = round(retries.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): InspectionRecord {
        return InspectionRecord(
            code = code,
            name = name,
            retries = retries,
            description = description,
            endsAt = endsAt,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a inspection record in status proposed. */
fun newInspectionRecord(code: String, name: String): InspectionRecord {
    return InspectionRecord(
        code = code,
        name = name,
        retries = 0,
        description = "",
        endsAt = 0,
        maximum = 0.0,
        status = InspectionRecordStatus.PROPOSED,
    )
}
