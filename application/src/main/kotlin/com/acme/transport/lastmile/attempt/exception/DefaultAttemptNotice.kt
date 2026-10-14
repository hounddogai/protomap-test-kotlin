package com.acme.transport.lastmile.attempt.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default attempt notice is in its life cycle. */
enum class DefaultAttemptNoticeStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A default attempt notice. Its code is unique within a tenant. */
data class DefaultAttemptNotice(
    var code: String,
    var name: String,
    var carrier: String,
    var unit: String,
    var capacity: Int,
    var margin: Double,
    var description: String,
    var amount: Double,
    var status: DefaultAttemptNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultAttemptNoticeStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultAttemptNoticeStatus): Boolean {
        when (status) {
            DefaultAttemptNoticeStatus.DRAFT -> return next == DefaultAttemptNoticeStatus.SUSPENDED
            DefaultAttemptNoticeStatus.PENDING -> {
                when (next) {
                    DefaultAttemptNoticeStatus.ACTIVE, DefaultAttemptNoticeStatus.CLOSED -> return true
                }
                return false
            }
            DefaultAttemptNoticeStatus.ACTIVE -> return next == DefaultAttemptNoticeStatus.SUSPENDED || next == DefaultAttemptNoticeStatus.CLOSED
            DefaultAttemptNoticeStatus.SUSPENDED -> return next == DefaultAttemptNoticeStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DefaultAttemptNoticeStatus) {
        if (!canMoveTo(next)) {
            error("default attempt notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default attempt notice"} $code"

    fun adjustedLoad(): Double = round(amount * margin * 100) / 100

    fun snapshot(): DefaultAttemptNotice {
        return DefaultAttemptNotice(
            code = code,
            name = name,
            carrier = carrier,
            unit = unit,
            capacity = capacity,
            margin = margin,
            description = description,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a default attempt notice in status draft. */
fun newDefaultAttemptNotice(code: String, name: String): DefaultAttemptNotice {
    return DefaultAttemptNotice(
        code = code,
        name = name,
        carrier = "",
        unit = "",
        capacity = 0,
        margin = 0.0,
        description = "",
        amount = 0.0,
        status = DefaultAttemptNoticeStatus.DRAFT,
    )
}
