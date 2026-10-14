package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual attempt assignment is in its life cycle. */
enum class ManualAttemptAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A manual attempt assignment. Its code is unique within a tenant. */
data class ManualAttemptAssignment(
    var code: String,
    var title: String,
    var segment: String,
    var stopCount: Int,
    var rate: Double,
    var batchSize: Int,
    var currency: String,
    var margin: Double,
    var country: String,
    var carrier: String,
    var lane: String,
    var status: ManualAttemptAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAttemptAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAttemptAssignmentStatus): Boolean {
        when (status) {
            ManualAttemptAssignmentStatus.CREATED -> return next == ManualAttemptAssignmentStatus.VALIDATED
            ManualAttemptAssignmentStatus.VALIDATED -> return next == ManualAttemptAssignmentStatus.RETIRED
            ManualAttemptAssignmentStatus.PUBLISHED -> return next == ManualAttemptAssignmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ManualAttemptAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("manual attempt assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedLoad(): Double = round(stopCount.toDouble() * rate * 100) / 100

    fun snapshot(): ManualAttemptAssignment {
        return ManualAttemptAssignment(
            code = code,
            title = title,
            segment = segment,
            stopCount = stopCount,
            rate = rate,
            batchSize = batchSize,
            currency = currency,
            margin = margin,
            country = country,
            carrier = carrier,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a manual attempt assignment in status created. */
fun newManualAttemptAssignment(code: String, title: String): ManualAttemptAssignment {
    return ManualAttemptAssignment(
        code = code,
        title = title,
        segment = "",
        stopCount = 0,
        rate = 0.0,
        batchSize = 0,
        currency = "",
        margin = 0.0,
        country = "",
        carrier = "",
        lane = "",
        status = ManualAttemptAssignmentStatus.CREATED,
    )
}
