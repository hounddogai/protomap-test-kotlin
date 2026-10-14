package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered declaration window records of customs's declaration work, and the rules, stores, and
// calculations around them.

/** Where a tiered declaration window is in its life cycle. */
enum class TieredDeclarationWindowStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a tiered declaration window and its current status. */
data class TieredDeclarationWindow(
    var code: String,
    var title: String,
    var attempts: Int,
    var notes: String,
    var updatedAt: Int,
    var priority: Int,
    var description: String,
    var status: TieredDeclarationWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDeclarationWindowStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDeclarationWindowStatus): Boolean {
        when (status) {
            TieredDeclarationWindowStatus.REQUESTED -> {
                when (next) {
                    TieredDeclarationWindowStatus.GRANTED -> return true
                }
                return false
            }
            TieredDeclarationWindowStatus.GRANTED -> {
                when (next) {
                    TieredDeclarationWindowStatus.REVOKED, TieredDeclarationWindowStatus.EXPIRED -> return true
                }
                return false
            }
            TieredDeclarationWindowStatus.REVOKED -> return next == TieredDeclarationWindowStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TieredDeclarationWindowStatus) {
        if (!canMoveTo(next)) {
            error("tiered declaration window $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netTotal(): Double = round((attempts.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): TieredDeclarationWindow {
        return TieredDeclarationWindow(
            code = code,
            title = title,
            attempts = attempts,
            notes = notes,
            updatedAt = updatedAt,
            priority = priority,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered declaration window in status requested. */
fun newTieredDeclarationWindow(code: String, title: String): TieredDeclarationWindow {
    return TieredDeclarationWindow(
        code = code,
        title = title,
        attempts = 0,
        notes = "",
        updatedAt = 0,
        priority = 0,
        description = "",
        status = TieredDeclarationWindowStatus.REQUESTED,
    )
}
