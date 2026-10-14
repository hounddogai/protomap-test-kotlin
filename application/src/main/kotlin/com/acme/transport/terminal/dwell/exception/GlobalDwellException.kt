package com.acme.transport.terminal.dwell.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global dwell exception records of terminal's dwell work, and the rules, stores, and calculations around
// them.

/** Where a global dwell exception is in its life cycle. */
enum class GlobalDwellExceptionStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A global dwell exception. Its code is unique within a tenant. */
data class GlobalDwellException(
    var code: String,
    var title: String,
    var version: Int,
    var stopCount: Int,
    var timezone: String,
    var createdAt: Int,
    var channel: String,
    var status: GlobalDwellExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDwellExceptionStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDwellExceptionStatus): Boolean {
        when (status) {
            GlobalDwellExceptionStatus.DRAFT -> return next == GlobalDwellExceptionStatus.PENDING || next == GlobalDwellExceptionStatus.SUSPENDED
            GlobalDwellExceptionStatus.PENDING -> return next == GlobalDwellExceptionStatus.ACTIVE
            GlobalDwellExceptionStatus.ACTIVE -> return next == GlobalDwellExceptionStatus.CLOSED
            GlobalDwellExceptionStatus.SUSPENDED -> return next == GlobalDwellExceptionStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: GlobalDwellExceptionStatus) {
        if (!canMoveTo(next)) {
            error("global dwell exception $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"global dwell exception"} $code"

    fun netValue(): Double = round((stopCount.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): GlobalDwellException {
        return GlobalDwellException(
            code = code,
            title = title,
            version = version,
            stopCount = stopCount,
            timezone = timezone,
            createdAt = createdAt,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a global dwell exception in status draft. */
fun newGlobalDwellException(code: String, title: String): GlobalDwellException {
    return GlobalDwellException(
        code = code,
        title = title,
        version = 0,
        stopCount = 0,
        timezone = "",
        createdAt = 0,
        channel = "",
        status = GlobalDwellExceptionStatus.DRAFT,
    )
}
