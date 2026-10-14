package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.kit.text.normalize

// The camera event claim records of safety's camera event work, and the rules, stores, and calculations
// around them.

/** Where a camera event claim is in its life cycle. */
enum class CameraEventClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a camera event claim and its current status. */
data class CameraEventClaim(
    var code: String,
    var label: String,
    var approved: Boolean,
    var summary: String,
    var sequence: Int,
    var depot: String,
    var status: CameraEventClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventClaimStatus): Boolean {
        when (status) {
            CameraEventClaimStatus.NEW -> return next == CameraEventClaimStatus.QUEUED
            CameraEventClaimStatus.QUEUED -> {
                when (next) {
                    CameraEventClaimStatus.RUNNING -> return true
                }
                return false
            }
            CameraEventClaimStatus.RUNNING -> return next == CameraEventClaimStatus.DONE || next == CameraEventClaimStatus.FAILED
            CameraEventClaimStatus.DONE -> {
                when (next) {
                    CameraEventClaimStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CameraEventClaimStatus) {
        if (!canMoveTo(next)) {
            error("camera event claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"camera event claim"} $code"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): CameraEventClaim {
        return CameraEventClaim(
            code = code,
            label = label,
            approved = approved,
            summary = summary,
            sequence = sequence,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a camera event claim in status new. */
fun newCameraEventClaim(code: String, label: String): CameraEventClaim {
    return CameraEventClaim(
        code = code,
        label = label,
        approved = false,
        summary = "",
        sequence = 0,
        depot = "",
        status = CameraEventClaimStatus.NEW,
    )
}
