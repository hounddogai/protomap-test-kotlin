package com.acme.transport.fleetops.utilization.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization assignment is in its life cycle. */
enum class UtilizationAssignmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A utilization assignment as the utilization team keeps it. */
data class UtilizationAssignment(
    var code: String,
    var title: String,
    var currency: String,
    var durationMinutes: Int,
    var ceiling: Double,
    var approved: Boolean,
    var expiresAt: Int,
    var priority: Int,
    var version: Int,
    var batchSize: Int,
    var minimum: Double,
    var status: UtilizationAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationAssignmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationAssignmentStatus): Boolean {
        when (status) {
            UtilizationAssignmentStatus.NEW -> return next == UtilizationAssignmentStatus.FAILED
            UtilizationAssignmentStatus.QUEUED -> return next == UtilizationAssignmentStatus.DONE
            UtilizationAssignmentStatus.RUNNING -> {
                when (next) {
                    UtilizationAssignmentStatus.DONE, UtilizationAssignmentStatus.FAILED -> return true
                }
                return false
            }
            UtilizationAssignmentStatus.DONE -> return next == UtilizationAssignmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: UtilizationAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("utilization assignment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun effectiveLoad(): Double = round(expiresAt.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): UtilizationAssignment {
        return UtilizationAssignment(
            code = code,
            title = title,
            currency = currency,
            durationMinutes = durationMinutes,
            ceiling = ceiling,
            approved = approved,
            expiresAt = expiresAt,
            priority = priority,
            version = version,
            batchSize = batchSize,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a utilization assignment in status new. */
fun newUtilizationAssignment(code: String, title: String): UtilizationAssignment {
    return UtilizationAssignment(
        code = code,
        title = title,
        currency = "",
        durationMinutes = 0,
        ceiling = 0.0,
        approved = false,
        expiresAt = 0,
        priority = 0,
        version = 0,
        batchSize = 0,
        minimum = 0.0,
        status = UtilizationAssignmentStatus.NEW,
    )
}
