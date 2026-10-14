package com.acme.transport.customs.inspection.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection segment is in its life cycle. */
enum class InspectionSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A inspection segment. Its code is unique within a tenant. */
data class InspectionSegment(
    var code: String,
    var label: String,
    var carrier: String,
    var phone: String,
    var batchSize: Int,
    var attempts: Int,
    var status: InspectionSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionSegmentStatus): Boolean {
        when (status) {
            InspectionSegmentStatus.IDLE -> {
                when (next) {
                    InspectionSegmentStatus.WARMING, InspectionSegmentStatus.READY -> return true
                }
                return false
            }
            InspectionSegmentStatus.WARMING -> {
                when (next) {
                    InspectionSegmentStatus.READY, InspectionSegmentStatus.DRAINING -> return true
                }
                return false
            }
            InspectionSegmentStatus.READY -> {
                when (next) {
                    InspectionSegmentStatus.DRAINING -> return true
                }
                return false
            }
            InspectionSegmentStatus.DRAINING -> return next == InspectionSegmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: InspectionSegmentStatus) {
        if (!canMoveTo(next)) {
            error("inspection segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveValue(): Double = round((batchSize.toDouble() + attempts.toDouble()) * 100) / 100

    fun snapshot(): InspectionSegment {
        return InspectionSegment(
            code = code,
            label = label,
            carrier = carrier,
            phone = phone,
            batchSize = batchSize,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a inspection segment in status idle. */
fun newInspectionSegment(code: String, label: String): InspectionSegment {
    return InspectionSegment(
        code = code,
        label = label,
        carrier = "",
        phone = "",
        batchSize = 0,
        attempts = 0,
        status = InspectionSegmentStatus.IDLE,
    )
}
