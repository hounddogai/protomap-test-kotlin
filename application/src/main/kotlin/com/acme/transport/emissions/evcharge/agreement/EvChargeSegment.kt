package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge segment is in its life cycle. */
enum class EvChargeSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A ev charge segment as the ev charge team keeps it. */
data class EvChargeSegment(
    var code: String,
    var name: String,
    var odometer: Int,
    var tags: MutableList<String>,
    var visible: Boolean,
    var updatedAt: Int,
    var etaMinutes: Int,
    var minimum: Double,
    var notes: String,
    var quantity: Int,
    var status: EvChargeSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeSegmentStatus): Boolean {
        when (status) {
            EvChargeSegmentStatus.OPEN -> {
                when (next) {
                    EvChargeSegmentStatus.IN_REVIEW, EvChargeSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
            EvChargeSegmentStatus.IN_REVIEW -> {
                when (next) {
                    EvChargeSegmentStatus.RESOLVED, EvChargeSegmentStatus.REOPENED -> return true
                }
                return false
            }
            EvChargeSegmentStatus.RESOLVED -> return next == EvChargeSegmentStatus.REOPENED || next == EvChargeSegmentStatus.ARCHIVED
            EvChargeSegmentStatus.REOPENED -> {
                when (next) {
                    EvChargeSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeSegmentStatus) {
        if (!canMoveTo(next)) {
            error("ev charge segment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round((etaMinutes.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): EvChargeSegment {
        return EvChargeSegment(
            code = code,
            name = name,
            odometer = odometer,
            tags = tags,
            visible = visible,
            updatedAt = updatedAt,
            etaMinutes = etaMinutes,
            minimum = minimum,
            notes = notes,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a ev charge segment in status open. */
fun newEvChargeSegment(code: String, name: String): EvChargeSegment {
    return EvChargeSegment(
        code = code,
        name = name,
        odometer = 0,
        tags = mutableListOf<String>(),
        visible = false,
        updatedAt = 0,
        etaMinutes = 0,
        minimum = 0.0,
        notes = "",
        quantity = 0,
        status = EvChargeSegmentStatus.OPEN,
    )
}
