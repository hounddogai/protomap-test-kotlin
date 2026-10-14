package com.acme.transport.fleetops.leasereturn.returnmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending lease return assignment is in its life cycle. */
enum class PendingLeaseReturnAssignmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A pending lease return assignment. Its code is unique within a tenant. */
data class PendingLeaseReturnAssignment(
    var code: String,
    var title: String,
    var source: String,
    var score: Double,
    var description: String,
    var routeCode: String,
    var distanceKm: Double,
    var retries: Int,
    var status: PendingLeaseReturnAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingLeaseReturnAssignmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingLeaseReturnAssignmentStatus): Boolean {
        when (status) {
            PendingLeaseReturnAssignmentStatus.NEW -> return next == PendingLeaseReturnAssignmentStatus.RUNNING || next == PendingLeaseReturnAssignmentStatus.DONE
            PendingLeaseReturnAssignmentStatus.QUEUED -> {
                when (next) {
                    PendingLeaseReturnAssignmentStatus.FAILED -> return true
                }
                return false
            }
            PendingLeaseReturnAssignmentStatus.RUNNING -> {
                when (next) {
                    PendingLeaseReturnAssignmentStatus.DONE, PendingLeaseReturnAssignmentStatus.FAILED -> return true
                }
                return false
            }
            PendingLeaseReturnAssignmentStatus.DONE -> {
                when (next) {
                    PendingLeaseReturnAssignmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingLeaseReturnAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("pending lease return assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending lease return assignment"} $code"

    fun netExposure(): Double = round((score + distanceKm) * 100) / 100

    fun snapshot(): PendingLeaseReturnAssignment {
        return PendingLeaseReturnAssignment(
            code = code,
            title = title,
            source = source,
            score = score,
            description = description,
            routeCode = routeCode,
            distanceKm = distanceKm,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a pending lease return assignment in status new. */
fun newPendingLeaseReturnAssignment(code: String, title: String): PendingLeaseReturnAssignment {
    return PendingLeaseReturnAssignment(
        code = code,
        title = title,
        source = "",
        score = 0.0,
        description = "",
        routeCode = "",
        distanceKm = 0.0,
        retries = 0,
        status = PendingLeaseReturnAssignmentStatus.NEW,
    )
}
