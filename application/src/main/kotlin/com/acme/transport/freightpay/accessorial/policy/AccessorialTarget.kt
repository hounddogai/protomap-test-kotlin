package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial target is in its life cycle. */
enum class AccessorialTargetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a accessorial target and its current status. */
data class AccessorialTarget(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var timezone: String,
    var dock: String,
    var description: String,
    var distanceKm: Double,
    var status: AccessorialTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialTargetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialTargetStatus): Boolean {
        when (status) {
            AccessorialTargetStatus.OPEN -> {
                when (next) {
                    AccessorialTargetStatus.IN_REVIEW, AccessorialTargetStatus.ARCHIVED -> return true
                }
                return false
            }
            AccessorialTargetStatus.IN_REVIEW -> return next == AccessorialTargetStatus.RESOLVED || next == AccessorialTargetStatus.ARCHIVED
            AccessorialTargetStatus.RESOLVED -> {
                when (next) {
                    AccessorialTargetStatus.ARCHIVED -> return true
                }
                return false
            }
            AccessorialTargetStatus.REOPENED -> {
                when (next) {
                    AccessorialTargetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialTargetStatus) {
        if (!canMoveTo(next)) {
            error("accessorial target $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"accessorial target"} $code"

    fun netExposure(): Double = round(distanceKm * updatedAt.toDouble() * 100) / 100

    fun snapshot(): AccessorialTarget {
        return AccessorialTarget(
            code = code,
            label = label,
            updatedAt = updatedAt,
            timezone = timezone,
            dock = dock,
            description = description,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a accessorial target in status open. */
fun newAccessorialTarget(code: String, label: String): AccessorialTarget {
    return AccessorialTarget(
        code = code,
        label = label,
        updatedAt = 0,
        timezone = "",
        dock = "",
        description = "",
        distanceKm = 0.0,
        status = AccessorialTargetStatus.OPEN,
    )
}
