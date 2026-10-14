package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default ev charge segment is in its life cycle. */
enum class DefaultEvChargeSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One default ev charge segment, with what the planners need to act on it. */
data class DefaultEvChargeSegment(
    var code: String,
    var label: String,
    var category: String,
    var unit: String,
    var dock: String,
    var locale: String,
    var createdAt: Int,
    var graceDays: Int,
    var weight: Double,
    var driverId: String,
    var description: String,
    var status: DefaultEvChargeSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultEvChargeSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultEvChargeSegmentStatus): Boolean {
        when (status) {
            DefaultEvChargeSegmentStatus.PLANNED -> {
                when (next) {
                    DefaultEvChargeSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultEvChargeSegmentStatus.SCHEDULED -> return next == DefaultEvChargeSegmentStatus.COMPLETED
            DefaultEvChargeSegmentStatus.IN_PROGRESS -> {
                when (next) {
                    DefaultEvChargeSegmentStatus.COMPLETED -> return true
                }
                return false
            }
            DefaultEvChargeSegmentStatus.COMPLETED -> {
                when (next) {
                    DefaultEvChargeSegmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultEvChargeSegmentStatus) {
        if (!canMoveTo(next)) {
            error("default ev charge segment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"default ev charge segment"} $code"

    fun netExposure(): Double = round((weight + createdAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultEvChargeSegment {
        return DefaultEvChargeSegment(
            code = code,
            label = label,
            category = category,
            unit = unit,
            dock = dock,
            locale = locale,
            createdAt = createdAt,
            graceDays = graceDays,
            weight = weight,
            driverId = driverId,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a default ev charge segment in status planned. */
fun newDefaultEvChargeSegment(code: String, label: String): DefaultEvChargeSegment {
    return DefaultEvChargeSegment(
        code = code,
        label = label,
        category = "",
        unit = "",
        dock = "",
        locale = "",
        createdAt = 0,
        graceDays = 0,
        weight = 0.0,
        driverId = "",
        description = "",
        status = DefaultEvChargeSegmentStatus.PLANNED,
    )
}
