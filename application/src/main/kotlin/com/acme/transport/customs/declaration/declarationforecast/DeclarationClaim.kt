package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration claim is in its life cycle. */
enum class DeclarationClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a declaration claim and its current status. */
data class DeclarationClaim(
    var code: String,
    var name: String,
    var endsAt: Int,
    var version: Int,
    var locale: String,
    var lane: String,
    var sequence: Int,
    var dock: String,
    var currency: String,
    var capacity: Int,
    var amount: Double,
    var status: DeclarationClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationClaimStatus): Boolean {
        when (status) {
            DeclarationClaimStatus.OPEN -> return next == DeclarationClaimStatus.ARCHIVED
            DeclarationClaimStatus.IN_REVIEW -> {
                when (next) {
                    DeclarationClaimStatus.REOPENED, DeclarationClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            DeclarationClaimStatus.RESOLVED -> return next == DeclarationClaimStatus.REOPENED || next == DeclarationClaimStatus.ARCHIVED
            DeclarationClaimStatus.REOPENED -> {
                when (next) {
                    DeclarationClaimStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationClaimStatus) {
        if (!canMoveTo(next)) {
            error("declaration claim $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code / $name"

    fun projectedVolume(): Double = round(amount * sequence.toDouble() * 100) / 100

    fun snapshot(): DeclarationClaim {
        return DeclarationClaim(
            code = code,
            name = name,
            endsAt = endsAt,
            version = version,
            locale = locale,
            lane = lane,
            sequence = sequence,
            dock = dock,
            currency = currency,
            capacity = capacity,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a declaration claim in status open. */
fun newDeclarationClaim(code: String, name: String): DeclarationClaim {
    return DeclarationClaim(
        code = code,
        name = name,
        endsAt = 0,
        version = 0,
        locale = "",
        lane = "",
        sequence = 0,
        dock = "",
        currency = "",
        capacity = 0,
        amount = 0.0,
        status = DeclarationClaimStatus.OPEN,
    )
}
