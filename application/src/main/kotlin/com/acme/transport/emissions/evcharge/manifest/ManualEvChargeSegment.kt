package com.acme.transport.emissions.evcharge.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual ev charge segment is in its life cycle. */
enum class ManualEvChargeSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a manual ev charge segment and its current status. */
data class ManualEvChargeSegment(
    var code: String,
    var name: String,
    var quantity: Int,
    var retries: Int,
    var source: String,
    var segment: String,
    var status: ManualEvChargeSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualEvChargeSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualEvChargeSegmentStatus): Boolean {
        when (status) {
            ManualEvChargeSegmentStatus.PLANNED -> return next == ManualEvChargeSegmentStatus.COMPLETED
            ManualEvChargeSegmentStatus.SCHEDULED -> {
                when (next) {
                    ManualEvChargeSegmentStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ManualEvChargeSegmentStatus.IN_PROGRESS -> return next == ManualEvChargeSegmentStatus.COMPLETED || next == ManualEvChargeSegmentStatus.CANCELLED
            ManualEvChargeSegmentStatus.COMPLETED -> {
                when (next) {
                    ManualEvChargeSegmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualEvChargeSegmentStatus) {
        if (!canMoveTo(next)) {
            error("manual ev charge segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"manual ev charge segment"} $code"

    fun weightedValue(): Double = round(retries.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): ManualEvChargeSegment {
        return ManualEvChargeSegment(
            code = code,
            name = name,
            quantity = quantity,
            retries = retries,
            source = source,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a manual ev charge segment in status planned. */
fun newManualEvChargeSegment(code: String, name: String): ManualEvChargeSegment {
    return ManualEvChargeSegment(
        code = code,
        name = name,
        quantity = 0,
        retries = 0,
        source = "",
        segment = "",
        status = ManualEvChargeSegmentStatus.PLANNED,
    )
}
