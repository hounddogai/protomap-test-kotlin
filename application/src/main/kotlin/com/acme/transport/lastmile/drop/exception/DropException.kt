package com.acme.transport.lastmile.drop.exception

import com.acme.transport.kit.text.normalize

// The drop exception records of lastmile's drop work, and the rules, stores, and calculations around them.

/** Where a drop exception is in its life cycle. */
enum class DropExceptionStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A drop exception as the drop team keeps it. */
data class DropException(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var source: String,
    var durationMinutes: Int,
    var channel: String,
    var status: DropExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropExceptionStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropExceptionStatus): Boolean {
        when (status) {
            DropExceptionStatus.OPEN -> {
                when (next) {
                    DropExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
            DropExceptionStatus.IN_REVIEW -> {
                when (next) {
                    DropExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
            DropExceptionStatus.RESOLVED -> {
                when (next) {
                    DropExceptionStatus.REOPENED, DropExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
            DropExceptionStatus.REOPENED -> return next == DropExceptionStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DropExceptionStatus) {
        if (!canMoveTo(next)) {
            error("drop exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"drop exception"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): DropException {
        return DropException(
            code = code,
            name = name,
            attributes = attributes,
            source = source,
            durationMinutes = durationMinutes,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a drop exception in status open. */
fun newDropException(code: String, name: String): DropException {
    return DropException(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        source = "",
        durationMinutes = 0,
        channel = "",
        status = DropExceptionStatus.OPEN,
    )
}
