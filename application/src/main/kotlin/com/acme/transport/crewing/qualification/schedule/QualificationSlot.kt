package com.acme.transport.crewing.qualification.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification slot is in its life cycle. */
enum class QualificationSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A qualification slot as the qualification team keeps it. */
data class QualificationSlot(
    var code: String,
    var title: String,
    var driverId: String,
    var unit: String,
    var country: String,
    var visible: Boolean,
    var margin: Double,
    var ownerId: String,
    var label: String,
    var channel: String,
    var capacity: Int,
    var status: QualificationSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationSlotStatus): Boolean {
        when (status) {
            QualificationSlotStatus.OPEN -> return next == QualificationSlotStatus.RESOLVED
            QualificationSlotStatus.IN_REVIEW -> {
                when (next) {
                    QualificationSlotStatus.RESOLVED -> return true
                }
                return false
            }
            QualificationSlotStatus.RESOLVED -> {
                when (next) {
                    QualificationSlotStatus.REOPENED -> return true
                }
                return false
            }
            QualificationSlotStatus.REOPENED -> {
                when (next) {
                    QualificationSlotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: QualificationSlotStatus) {
        if (!canMoveTo(next)) {
            error("qualification slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"qualification slot"} $code"

    fun effectiveTotal(): Double = round((capacity.toDouble() - margin) * 100) / 100

    fun snapshot(): QualificationSlot {
        return QualificationSlot(
            code = code,
            title = title,
            driverId = driverId,
            unit = unit,
            country = country,
            visible = visible,
            margin = margin,
            ownerId = ownerId,
            label = label,
            channel = channel,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a qualification slot in status open. */
fun newQualificationSlot(code: String, title: String): QualificationSlot {
    return QualificationSlot(
        code = code,
        title = title,
        driverId = "",
        unit = "",
        country = "",
        visible = false,
        margin = 0.0,
        ownerId = "",
        label = "",
        channel = "",
        capacity = 0,
        status = QualificationSlotStatus.OPEN,
    )
}
