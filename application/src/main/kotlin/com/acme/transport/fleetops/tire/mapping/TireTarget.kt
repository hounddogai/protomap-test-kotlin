package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire target is in its life cycle. */
enum class TireTargetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A tire target as the tire team keeps it. */
data class TireTarget(
    var code: String,
    var label: String,
    var maximum: Double,
    var attempts: Int,
    var rate: Double,
    var unit: String,
    var approved: Boolean,
    var region: String,
    var amount: Double,
    var phone: String,
    var status: TireTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireTargetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireTargetStatus): Boolean {
        when (status) {
            TireTargetStatus.DRAFT -> {
                when (next) {
                    TireTargetStatus.PENDING -> return true
                }
                return false
            }
            TireTargetStatus.PENDING -> {
                when (next) {
                    TireTargetStatus.SUSPENDED -> return true
                }
                return false
            }
            TireTargetStatus.ACTIVE -> {
                when (next) {
                    TireTargetStatus.SUSPENDED, TireTargetStatus.CLOSED -> return true
                }
                return false
            }
            TireTargetStatus.SUSPENDED -> return next == TireTargetStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TireTargetStatus) {
        if (!canMoveTo(next)) {
            error("tire target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tire target"} $code"

    fun projectedExposure(): Double = round((attempts.toDouble() + maximum) * 100) / 100

    fun snapshot(): TireTarget {
        return TireTarget(
            code = code,
            label = label,
            maximum = maximum,
            attempts = attempts,
            rate = rate,
            unit = unit,
            approved = approved,
            region = region,
            amount = amount,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a tire target in status draft. */
fun newTireTarget(code: String, label: String): TireTarget {
    return TireTarget(
        code = code,
        label = label,
        maximum = 0.0,
        attempts = 0,
        rate = 0.0,
        unit = "",
        approved = false,
        region = "",
        amount = 0.0,
        phone = "",
        status = TireTargetStatus.DRAFT,
    )
}
