package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling trailer assignment is in its life cycle. */
enum class RollingTrailerAssignmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rolling trailer assignment as the trailer team keeps it. */
data class RollingTrailerAssignment(
    var code: String,
    var name: String,
    var odometer: Int,
    var minimum: Double,
    var currency: String,
    var threshold: Double,
    var phone: String,
    var ceiling: Double,
    var graceDays: Int,
    var region: String,
    var status: RollingTrailerAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTrailerAssignmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTrailerAssignmentStatus): Boolean {
        when (status) {
            RollingTrailerAssignmentStatus.NEW -> return next == RollingTrailerAssignmentStatus.DONE || next == RollingTrailerAssignmentStatus.FAILED
            RollingTrailerAssignmentStatus.QUEUED -> return next == RollingTrailerAssignmentStatus.RUNNING || next == RollingTrailerAssignmentStatus.FAILED
            RollingTrailerAssignmentStatus.RUNNING -> return next == RollingTrailerAssignmentStatus.DONE || next == RollingTrailerAssignmentStatus.FAILED
            RollingTrailerAssignmentStatus.DONE -> return next == RollingTrailerAssignmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingTrailerAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("rolling trailer assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedVolume(): Double = round(odometer.toDouble() * minimum * 100) / 100

    fun snapshot(): RollingTrailerAssignment {
        return RollingTrailerAssignment(
            code = code,
            name = name,
            odometer = odometer,
            minimum = minimum,
            currency = currency,
            threshold = threshold,
            phone = phone,
            ceiling = ceiling,
            graceDays = graceDays,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rolling trailer assignment in status new. */
fun newRollingTrailerAssignment(code: String, name: String): RollingTrailerAssignment {
    return RollingTrailerAssignment(
        code = code,
        name = name,
        odometer = 0,
        minimum = 0.0,
        currency = "",
        threshold = 0.0,
        phone = "",
        ceiling = 0.0,
        graceDays = 0,
        region = "",
        status = RollingTrailerAssignmentStatus.NEW,
    )
}
