package com.acme.transport.fleetops.defect.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The defect notice records of fleetops's defect work, and the rules, stores, and calculations around them.

/** Where a defect notice is in its life cycle. */
enum class DefectNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A defect notice. Its code is unique within a tenant. */
data class DefectNotice(
    var code: String,
    var label: String,
    var capacity: Int,
    var vehicleId: String,
    var margin: Double,
    var description: String,
    var status: DefectNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectNoticeStatus): Boolean {
        when (status) {
            DefectNoticeStatus.PROPOSED -> {
                when (next) {
                    DefectNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefectNoticeStatus.APPROVED -> return next == DefectNoticeStatus.WITHDRAWN
            DefectNoticeStatus.REJECTED -> return next == DefectNoticeStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DefectNoticeStatus) {
        if (!canMoveTo(next)) {
            error("defect notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"defect notice"} $code"

    fun effectiveExposure(): Double = round((capacity.toDouble() - margin) * 100) / 100

    fun snapshot(): DefectNotice {
        return DefectNotice(
            code = code,
            label = label,
            capacity = capacity,
            vehicleId = vehicleId,
            margin = margin,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a defect notice in status proposed. */
fun newDefectNotice(code: String, label: String): DefectNotice {
    return DefectNotice(
        code = code,
        label = label,
        capacity = 0,
        vehicleId = "",
        margin = 0.0,
        description = "",
        status = DefectNoticeStatus.PROPOSED,
    )
}
