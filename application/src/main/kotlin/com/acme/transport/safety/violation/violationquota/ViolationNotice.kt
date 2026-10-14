package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation notice is in its life cycle. */
enum class ViolationNoticeStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A violation notice. Its code is unique within a tenant. */
data class ViolationNotice(
    var code: String,
    var label: String,
    var maximum: Double,
    var unit: String,
    var region: String,
    var limit: Int,
    var status: ViolationNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationNoticeStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationNoticeStatus): Boolean {
        when (status) {
            ViolationNoticeStatus.PLANNED -> {
                when (next) {
                    ViolationNoticeStatus.CANCELLED -> return true
                }
                return false
            }
            ViolationNoticeStatus.SCHEDULED -> {
                when (next) {
                    ViolationNoticeStatus.COMPLETED, ViolationNoticeStatus.CANCELLED -> return true
                }
                return false
            }
            ViolationNoticeStatus.IN_PROGRESS -> return next == ViolationNoticeStatus.COMPLETED || next == ViolationNoticeStatus.CANCELLED
            ViolationNoticeStatus.COMPLETED -> {
                when (next) {
                    ViolationNoticeStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ViolationNoticeStatus) {
        if (!canMoveTo(next)) {
            error("violation notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun netTotal(): Double = round((limit.toDouble() - maximum) * 100) / 100

    fun snapshot(): ViolationNotice {
        return ViolationNotice(
            code = code,
            label = label,
            maximum = maximum,
            unit = unit,
            region = region,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a violation notice in status planned. */
fun newViolationNotice(code: String, label: String): ViolationNotice {
    return ViolationNotice(
        code = code,
        label = label,
        maximum = 0.0,
        unit = "",
        region = "",
        limit = 0,
        status = ViolationNoticeStatus.PLANNED,
    )
}
