package com.acme.transport.customs.declaration.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration target is in its life cycle. */
enum class DeclarationTargetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a declaration target and its current status. */
data class DeclarationTarget(
    var code: String,
    var title: String,
    var vehicleId: String,
    var pinned: Boolean,
    var name: String,
    var shiftCode: String,
    var country: String,
    var budget: Double,
    var cost: Double,
    var status: DeclarationTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationTargetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationTargetStatus): Boolean {
        when (status) {
            DeclarationTargetStatus.DRAFT -> {
                when (next) {
                    DeclarationTargetStatus.PENDING -> return true
                }
                return false
            }
            DeclarationTargetStatus.PENDING -> {
                when (next) {
                    DeclarationTargetStatus.SUSPENDED -> return true
                }
                return false
            }
            DeclarationTargetStatus.ACTIVE -> {
                when (next) {
                    DeclarationTargetStatus.SUSPENDED, DeclarationTargetStatus.CLOSED -> return true
                }
                return false
            }
            DeclarationTargetStatus.SUSPENDED -> return next == DeclarationTargetStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DeclarationTargetStatus) {
        if (!canMoveTo(next)) {
            error("declaration target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"declaration target"} $code"

    fun adjustedExposure(): Double = round(budget * cost * 100) / 100

    fun snapshot(): DeclarationTarget {
        return DeclarationTarget(
            code = code,
            title = title,
            vehicleId = vehicleId,
            pinned = pinned,
            name = name,
            shiftCode = shiftCode,
            country = country,
            budget = budget,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a declaration target in status draft. */
fun newDeclarationTarget(code: String, title: String): DeclarationTarget {
    return DeclarationTarget(
        code = code,
        title = title,
        vehicleId = "",
        pinned = false,
        name = "",
        shiftCode = "",
        country = "",
        budget = 0.0,
        cost = 0.0,
        status = DeclarationTargetStatus.DRAFT,
    )
}
