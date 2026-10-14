package com.acme.transport.terminal.traileryard.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard audit is in its life cycle. */
enum class TrailerYardAuditStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A trailer yard audit. Its code is unique within a tenant. */
data class TrailerYardAudit(
    var code: String,
    var name: String,
    var odometer: Int,
    var maximum: Double,
    var floorValue: Double,
    var weight: Double,
    var priority: Int,
    var region: String,
    var contactEmail: String,
    var lane: String,
    var status: TrailerYardAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardAuditStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardAuditStatus): Boolean {
        when (status) {
            TrailerYardAuditStatus.PROPOSED -> return next == TrailerYardAuditStatus.REJECTED
            TrailerYardAuditStatus.APPROVED -> return next == TrailerYardAuditStatus.REJECTED || next == TrailerYardAuditStatus.WITHDRAWN
            TrailerYardAuditStatus.REJECTED -> {
                when (next) {
                    TrailerYardAuditStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerYardAuditStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedVolume(): Double = round((priority.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): TrailerYardAudit {
        return TrailerYardAudit(
            code = code,
            name = name,
            odometer = odometer,
            maximum = maximum,
            floorValue = floorValue,
            weight = weight,
            priority = priority,
            region = region,
            contactEmail = contactEmail,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a trailer yard audit in status proposed. */
fun newTrailerYardAudit(code: String, name: String): TrailerYardAudit {
    return TrailerYardAudit(
        code = code,
        name = name,
        odometer = 0,
        maximum = 0.0,
        floorValue = 0.0,
        weight = 0.0,
        priority = 0,
        region = "",
        contactEmail = "",
        lane = "",
        status = TrailerYardAuditStatus.PROPOSED,
    )
}
