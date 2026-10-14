package com.acme.transport.customs.declaration.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration override is in its life cycle. */
enum class DeclarationOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a declaration override and its current status. */
data class DeclarationOverride(
    var code: String,
    var name: String,
    var discount: Double,
    var vehicleId: String,
    var amount: Double,
    var status: DeclarationOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationOverrideStatus): Boolean {
        when (status) {
            DeclarationOverrideStatus.OPEN -> {
                when (next) {
                    DeclarationOverrideStatus.REOPENED, DeclarationOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
            DeclarationOverrideStatus.IN_REVIEW -> return next == DeclarationOverrideStatus.RESOLVED || next == DeclarationOverrideStatus.ARCHIVED
            DeclarationOverrideStatus.RESOLVED -> return next == DeclarationOverrideStatus.REOPENED || next == DeclarationOverrideStatus.ARCHIVED
            DeclarationOverrideStatus.REOPENED -> {
                when (next) {
                    DeclarationOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationOverrideStatus) {
        if (!canMoveTo(next)) {
            error("declaration override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"declaration override"} $code"

    fun adjustedVolume(): Double = round((amount - discount) * 100) / 100

    fun snapshot(): DeclarationOverride {
        return DeclarationOverride(
            code = code,
            name = name,
            discount = discount,
            vehicleId = vehicleId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a declaration override in status open. */
fun newDeclarationOverride(code: String, name: String): DeclarationOverride {
    return DeclarationOverride(
        code = code,
        name = name,
        discount = 0.0,
        vehicleId = "",
        amount = 0.0,
        status = DeclarationOverrideStatus.OPEN,
    )
}
