package com.acme.transport.fleetops.utilization.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary utilization assignment is in its life cycle. */
enum class PrimaryUtilizationAssignmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A primary utilization assignment as the utilization team keeps it. */
data class PrimaryUtilizationAssignment(
    var code: String,
    var title: String,
    var timezone: String,
    var category: String,
    var endsAt: Int,
    var limit: Int,
    var weight: Double,
    var notes: String,
    var loadKg: Double,
    var odometer: Int,
    var status: PrimaryUtilizationAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryUtilizationAssignmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryUtilizationAssignmentStatus): Boolean {
        when (status) {
            PrimaryUtilizationAssignmentStatus.NEW -> return next == PrimaryUtilizationAssignmentStatus.QUEUED || next == PrimaryUtilizationAssignmentStatus.RUNNING
            PrimaryUtilizationAssignmentStatus.QUEUED -> {
                when (next) {
                    PrimaryUtilizationAssignmentStatus.RUNNING, PrimaryUtilizationAssignmentStatus.FAILED -> return true
                }
                return false
            }
            PrimaryUtilizationAssignmentStatus.RUNNING -> return next == PrimaryUtilizationAssignmentStatus.FAILED
            PrimaryUtilizationAssignmentStatus.DONE -> {
                when (next) {
                    PrimaryUtilizationAssignmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryUtilizationAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("primary utilization assignment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun adjustedExposure(): Double = round(loadKg * odometer.toDouble() * 100) / 100

    fun snapshot(): PrimaryUtilizationAssignment {
        return PrimaryUtilizationAssignment(
            code = code,
            title = title,
            timezone = timezone,
            category = category,
            endsAt = endsAt,
            limit = limit,
            weight = weight,
            notes = notes,
            loadKg = loadKg,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a primary utilization assignment in status new. */
fun newPrimaryUtilizationAssignment(code: String, title: String): PrimaryUtilizationAssignment {
    return PrimaryUtilizationAssignment(
        code = code,
        title = title,
        timezone = "",
        category = "",
        endsAt = 0,
        limit = 0,
        weight = 0.0,
        notes = "",
        loadKg = 0.0,
        odometer = 0,
        status = PrimaryUtilizationAssignmentStatus.NEW,
    )
}
