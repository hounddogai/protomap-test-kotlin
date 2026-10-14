package com.acme.transport.terminal.crossdock.dockslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy cross dock exception is in its life cycle. */
enum class LegacyCrossDockExceptionStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A legacy cross dock exception as the cross dock team keeps it. */
data class LegacyCrossDockException(
    var code: String,
    var title: String,
    var pallets: Int,
    var ownerId: String,
    var sequence: Int,
    var externalRef: String,
    var dock: String,
    var threshold: Double,
    var amount: Double,
    var locale: String,
    var status: LegacyCrossDockExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCrossDockExceptionStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCrossDockExceptionStatus): Boolean {
        when (status) {
            LegacyCrossDockExceptionStatus.DRAFT -> return next == LegacyCrossDockExceptionStatus.PENDING
            LegacyCrossDockExceptionStatus.PENDING -> {
                when (next) {
                    LegacyCrossDockExceptionStatus.ACTIVE -> return true
                }
                return false
            }
            LegacyCrossDockExceptionStatus.ACTIVE -> return next == LegacyCrossDockExceptionStatus.SUSPENDED
            LegacyCrossDockExceptionStatus.SUSPENDED -> {
                when (next) {
                    LegacyCrossDockExceptionStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyCrossDockExceptionStatus) {
        if (!canMoveTo(next)) {
            error("legacy cross dock exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveLoad(): Double = round((threshold + amount) * 100) / 100

    fun snapshot(): LegacyCrossDockException {
        return LegacyCrossDockException(
            code = code,
            title = title,
            pallets = pallets,
            ownerId = ownerId,
            sequence = sequence,
            externalRef = externalRef,
            dock = dock,
            threshold = threshold,
            amount = amount,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a legacy cross dock exception in status draft. */
fun newLegacyCrossDockException(code: String, title: String): LegacyCrossDockException {
    return LegacyCrossDockException(
        code = code,
        title = title,
        pallets = 0,
        ownerId = "",
        sequence = 0,
        externalRef = "",
        dock = "",
        threshold = 0.0,
        amount = 0.0,
        locale = "",
        status = LegacyCrossDockExceptionStatus.DRAFT,
    )
}
