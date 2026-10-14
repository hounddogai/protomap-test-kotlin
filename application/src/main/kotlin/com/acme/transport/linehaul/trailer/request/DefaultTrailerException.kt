package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default trailer exception is in its life cycle. */
enum class DefaultTrailerExceptionStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a default trailer exception and its current status. */
data class DefaultTrailerException(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var createdAt: Int,
    var contactEmail: String,
    var limit: Int,
    var archived: Boolean,
    var carrier: String,
    var updatedAt: Int,
    var status: DefaultTrailerExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTrailerExceptionStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTrailerExceptionStatus): Boolean {
        when (status) {
            DefaultTrailerExceptionStatus.PROPOSED -> return next == DefaultTrailerExceptionStatus.WITHDRAWN
            DefaultTrailerExceptionStatus.APPROVED -> {
                when (next) {
                    DefaultTrailerExceptionStatus.REJECTED -> return true
                }
                return false
            }
            DefaultTrailerExceptionStatus.REJECTED -> return next == DefaultTrailerExceptionStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DefaultTrailerExceptionStatus) {
        if (!canMoveTo(next)) {
            error("default trailer exception $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun netValue(): Double = round((updatedAt.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultTrailerException {
        return DefaultTrailerException(
            code = code,
            label = label,
            expiresAt = expiresAt,
            createdAt = createdAt,
            contactEmail = contactEmail,
            limit = limit,
            archived = archived,
            carrier = carrier,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default trailer exception in status proposed. */
fun newDefaultTrailerException(code: String, label: String): DefaultTrailerException {
    return DefaultTrailerException(
        code = code,
        label = label,
        expiresAt = 0,
        createdAt = 0,
        contactEmail = "",
        limit = 0,
        archived = false,
        carrier = "",
        updatedAt = 0,
        status = DefaultTrailerExceptionStatus.PROPOSED,
    )
}
