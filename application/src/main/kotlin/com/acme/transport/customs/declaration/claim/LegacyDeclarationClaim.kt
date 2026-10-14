package com.acme.transport.customs.declaration.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy declaration claim records of customs's declaration work, and the rules, stores, and calculations
// around them.

/** Where a legacy declaration claim is in its life cycle. */
enum class LegacyDeclarationClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy declaration claim as the declaration team keeps it. */
data class LegacyDeclarationClaim(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var maximum: Double,
    var expiresAt: Int,
    var version: Int,
    var attempts: Int,
    var amount: Double,
    var status: LegacyDeclarationClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDeclarationClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDeclarationClaimStatus): Boolean {
        when (status) {
            LegacyDeclarationClaimStatus.NEW -> return next == LegacyDeclarationClaimStatus.FAILED
            LegacyDeclarationClaimStatus.QUEUED -> return next == LegacyDeclarationClaimStatus.FAILED
            LegacyDeclarationClaimStatus.RUNNING -> return next == LegacyDeclarationClaimStatus.DONE
            LegacyDeclarationClaimStatus.DONE -> return next == LegacyDeclarationClaimStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LegacyDeclarationClaimStatus) {
        if (!canMoveTo(next)) {
            error("legacy declaration claim $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code / $name"

    fun adjustedLoad(): Double = round(fuelLiters * maximum * 100) / 100

    fun snapshot(): LegacyDeclarationClaim {
        return LegacyDeclarationClaim(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            maximum = maximum,
            expiresAt = expiresAt,
            version = version,
            attempts = attempts,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy declaration claim in status new. */
fun newLegacyDeclarationClaim(code: String, name: String): LegacyDeclarationClaim {
    return LegacyDeclarationClaim(
        code = code,
        name = name,
        fuelLiters = 0.0,
        maximum = 0.0,
        expiresAt = 0,
        version = 0,
        attempts = 0,
        amount = 0.0,
        status = LegacyDeclarationClaimStatus.NEW,
    )
}
