package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.kit.text.normalize

/** Where a tiered declaration score is in its life cycle. */
enum class TieredDeclarationScoreStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One tiered declaration score, with what the planners need to act on it. */
data class TieredDeclarationScore(
    var code: String,
    var label: String,
    var phone: String,
    var approved: Boolean,
    var capacity: Int,
    var status: TieredDeclarationScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDeclarationScoreStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDeclarationScoreStatus): Boolean {
        when (status) {
            TieredDeclarationScoreStatus.DRAFT -> return next == TieredDeclarationScoreStatus.PENDING
            TieredDeclarationScoreStatus.PENDING -> return next == TieredDeclarationScoreStatus.ACTIVE
            TieredDeclarationScoreStatus.ACTIVE -> return next == TieredDeclarationScoreStatus.SUSPENDED
            TieredDeclarationScoreStatus.SUSPENDED -> {
                when (next) {
                    TieredDeclarationScoreStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredDeclarationScoreStatus) {
        if (!canMoveTo(next)) {
            error("tiered declaration score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tiered declaration score"} $code"

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): TieredDeclarationScore {
        return TieredDeclarationScore(
            code = code,
            label = label,
            phone = phone,
            approved = approved,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered declaration score in status draft. */
fun newTieredDeclarationScore(code: String, label: String): TieredDeclarationScore {
    return TieredDeclarationScore(
        code = code,
        label = label,
        phone = "",
        approved = false,
        capacity = 0,
        status = TieredDeclarationScoreStatus.DRAFT,
    )
}
