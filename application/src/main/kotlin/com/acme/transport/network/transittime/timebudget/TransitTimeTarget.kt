package com.acme.transport.network.transittime.timebudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time target is in its life cycle. */
enum class TransitTimeTargetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One transit time target, with what the planners need to act on it. */
data class TransitTimeTarget(
    var code: String,
    var label: String,
    var externalRef: String,
    var unit: String,
    var durationMinutes: Int,
    var weight: Double,
    var carrier: String,
    var quantity: Int,
    var score: Double,
    var reviewer: String,
    var attempts: Int,
    var status: TransitTimeTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeTargetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeTargetStatus): Boolean {
        when (status) {
            TransitTimeTargetStatus.REQUESTED -> {
                when (next) {
                    TransitTimeTargetStatus.EXPIRED -> return true
                }
                return false
            }
            TransitTimeTargetStatus.GRANTED -> {
                when (next) {
                    TransitTimeTargetStatus.REVOKED, TransitTimeTargetStatus.EXPIRED -> return true
                }
                return false
            }
            TransitTimeTargetStatus.REVOKED -> return next == TransitTimeTargetStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TransitTimeTargetStatus) {
        if (!canMoveTo(next)) {
            error("transit time target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedTotal(): Double = round((durationMinutes.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): TransitTimeTarget {
        return TransitTimeTarget(
            code = code,
            label = label,
            externalRef = externalRef,
            unit = unit,
            durationMinutes = durationMinutes,
            weight = weight,
            carrier = carrier,
            quantity = quantity,
            score = score,
            reviewer = reviewer,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a transit time target in status requested. */
fun newTransitTimeTarget(code: String, label: String): TransitTimeTarget {
    return TransitTimeTarget(
        code = code,
        label = label,
        externalRef = "",
        unit = "",
        durationMinutes = 0,
        weight = 0.0,
        carrier = "",
        quantity = 0,
        score = 0.0,
        reviewer = "",
        attempts = 0,
        status = TransitTimeTargetStatus.REQUESTED,
    )
}
