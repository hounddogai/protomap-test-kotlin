package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration score is in its life cycle. */
enum class DeclarationScoreStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A declaration score as the declaration team keeps it. */
data class DeclarationScore(
    var code: String,
    var title: String,
    var cost: Double,
    var updatedAt: Int,
    var name: String,
    var status: DeclarationScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationScoreStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationScoreStatus): Boolean {
        when (status) {
            DeclarationScoreStatus.REQUESTED -> {
                when (next) {
                    DeclarationScoreStatus.REVOKED, DeclarationScoreStatus.EXPIRED -> return true
                }
                return false
            }
            DeclarationScoreStatus.GRANTED -> return next == DeclarationScoreStatus.REVOKED || next == DeclarationScoreStatus.EXPIRED
            DeclarationScoreStatus.REVOKED -> return next == DeclarationScoreStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DeclarationScoreStatus) {
        if (!canMoveTo(next)) {
            error("declaration score $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun netLoad(): Double = round(cost * updatedAt.toDouble() * 100) / 100

    fun snapshot(): DeclarationScore {
        return DeclarationScore(
            code = code,
            title = title,
            cost = cost,
            updatedAt = updatedAt,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a declaration score in status requested. */
fun newDeclarationScore(code: String, title: String): DeclarationScore {
    return DeclarationScore(
        code = code,
        title = title,
        cost = 0.0,
        updatedAt = 0,
        name = "",
        status = DeclarationScoreStatus.REQUESTED,
    )
}
