package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery assignment is in its life cycle. */
enum class RedeliveryAssignmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a redelivery assignment and its current status. */
data class RedeliveryAssignment(
    var code: String,
    var label: String,
    var discount: Double,
    var channel: String,
    var durationMinutes: Int,
    var amount: Double,
    var country: String,
    var status: RedeliveryAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryAssignmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryAssignmentStatus): Boolean {
        when (status) {
            RedeliveryAssignmentStatus.NEW -> return next == RedeliveryAssignmentStatus.DONE || next == RedeliveryAssignmentStatus.FAILED
            RedeliveryAssignmentStatus.QUEUED -> {
                when (next) {
                    RedeliveryAssignmentStatus.RUNNING, RedeliveryAssignmentStatus.FAILED -> return true
                }
                return false
            }
            RedeliveryAssignmentStatus.RUNNING -> return next == RedeliveryAssignmentStatus.FAILED
            RedeliveryAssignmentStatus.DONE -> {
                when (next) {
                    RedeliveryAssignmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RedeliveryAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("redelivery assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"redelivery assignment"} $code"

    fun effectiveExposure(): Double = round((amount + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): RedeliveryAssignment {
        return RedeliveryAssignment(
            code = code,
            label = label,
            discount = discount,
            channel = channel,
            durationMinutes = durationMinutes,
            amount = amount,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a redelivery assignment in status new. */
fun newRedeliveryAssignment(code: String, label: String): RedeliveryAssignment {
    return RedeliveryAssignment(
        code = code,
        label = label,
        discount = 0.0,
        channel = "",
        durationMinutes = 0,
        amount = 0.0,
        country = "",
        status = RedeliveryAssignmentStatus.NEW,
    )
}
