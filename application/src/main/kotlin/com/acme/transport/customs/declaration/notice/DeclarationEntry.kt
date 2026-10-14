package com.acme.transport.customs.declaration.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration entry is in its life cycle. */
enum class DeclarationEntryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A declaration entry. Its code is unique within a tenant. */
data class DeclarationEntry(
    var code: String,
    var title: String,
    var phone: String,
    var cost: Double,
    var notes: String,
    var trailerId: String,
    var limit: Int,
    var status: DeclarationEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationEntryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationEntryStatus): Boolean {
        when (status) {
            DeclarationEntryStatus.IDLE -> return next == DeclarationEntryStatus.DRAINING || next == DeclarationEntryStatus.STOPPED
            DeclarationEntryStatus.WARMING -> return next == DeclarationEntryStatus.READY
            DeclarationEntryStatus.READY -> {
                when (next) {
                    DeclarationEntryStatus.DRAINING -> return true
                }
                return false
            }
            DeclarationEntryStatus.DRAINING -> return next == DeclarationEntryStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DeclarationEntryStatus) {
        if (!canMoveTo(next)) {
            error("declaration entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"declaration entry"} $code"

    fun effectiveExposure(): Double = round((cost - limit.toDouble()) * 100) / 100

    fun snapshot(): DeclarationEntry {
        return DeclarationEntry(
            code = code,
            title = title,
            phone = phone,
            cost = cost,
            notes = notes,
            trailerId = trailerId,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a declaration entry in status idle. */
fun newDeclarationEntry(code: String, title: String): DeclarationEntry {
    return DeclarationEntry(
        code = code,
        title = title,
        phone = "",
        cost = 0.0,
        notes = "",
        trailerId = "",
        limit = 0,
        status = DeclarationEntryStatus.IDLE,
    )
}
