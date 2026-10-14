package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.text.normalize

// The camera event audit records of safety's camera event work, and the rules, stores, and calculations
// around them.

/** Where a camera event audit is in its life cycle. */
enum class CameraEventAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a camera event audit and its current status. */
data class CameraEventAudit(
    var code: String,
    var label: String,
    var source: String,
    var title: String,
    var visible: Boolean,
    var vehicleId: String,
    var carrier: String,
    var segment: String,
    var currency: String,
    var discount: Double,
    var status: CameraEventAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventAuditStatus): Boolean {
        when (status) {
            CameraEventAuditStatus.OPEN -> {
                when (next) {
                    CameraEventAuditStatus.RESOLVED, CameraEventAuditStatus.REOPENED -> return true
                }
                return false
            }
            CameraEventAuditStatus.IN_REVIEW -> {
                when (next) {
                    CameraEventAuditStatus.REOPENED, CameraEventAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            CameraEventAuditStatus.RESOLVED -> return next == CameraEventAuditStatus.REOPENED
            CameraEventAuditStatus.REOPENED -> return next == CameraEventAuditStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CameraEventAuditStatus) {
        if (!canMoveTo(next)) {
            error("camera event audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = discount

    fun snapshot(): CameraEventAudit {
        return CameraEventAudit(
            code = code,
            label = label,
            source = source,
            title = title,
            visible = visible,
            vehicleId = vehicleId,
            carrier = carrier,
            segment = segment,
            currency = currency,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a camera event audit in status open. */
fun newCameraEventAudit(code: String, label: String): CameraEventAudit {
    return CameraEventAudit(
        code = code,
        label = label,
        source = "",
        title = "",
        visible = false,
        vehicleId = "",
        carrier = "",
        segment = "",
        currency = "",
        discount = 0.0,
        status = CameraEventAuditStatus.OPEN,
    )
}
