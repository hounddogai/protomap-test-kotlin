package com.acme.transport.lastmile.attempt.manifest

import com.acme.transport.kit.text.normalize

/** Where a attempt notice is in its life cycle. */
enum class AttemptNoticeStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One attempt notice, with what the planners need to act on it. */
data class AttemptNotice(
    var code: String,
    var label: String,
    var shiftCode: String,
    var discount: Double,
    var country: String,
    var status: AttemptNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptNoticeStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptNoticeStatus): Boolean {
        when (status) {
            AttemptNoticeStatus.DRAFT -> return next == AttemptNoticeStatus.PENDING || next == AttemptNoticeStatus.SUSPENDED
            AttemptNoticeStatus.PENDING -> return next == AttemptNoticeStatus.ACTIVE || next == AttemptNoticeStatus.SUSPENDED
            AttemptNoticeStatus.ACTIVE -> return next == AttemptNoticeStatus.SUSPENDED
            AttemptNoticeStatus.SUSPENDED -> return next == AttemptNoticeStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AttemptNoticeStatus) {
        if (!canMoveTo(next)) {
            error("attempt notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = discount

    fun snapshot(): AttemptNotice {
        return AttemptNotice(
            code = code,
            label = label,
            shiftCode = shiftCode,
            discount = discount,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a attempt notice in status draft. */
fun newAttemptNotice(code: String, label: String): AttemptNotice {
    return AttemptNotice(
        code = code,
        label = label,
        shiftCode = "",
        discount = 0.0,
        country = "",
        status = AttemptNoticeStatus.DRAFT,
    )
}
