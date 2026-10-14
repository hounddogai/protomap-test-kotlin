package com.acme.transport.linehaul.trailer.trailerrule

import com.acme.transport.kit.text.normalize

/** Where a trailer notice is in its life cycle. */
enum class TrailerNoticeStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One trailer notice, with what the planners need to act on it. */
data class TrailerNotice(
    var code: String,
    var name: String,
    var dock: String,
    var locale: String,
    var source: String,
    var amount: Double,
    var status: TrailerNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerNoticeStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerNoticeStatus): Boolean {
        when (status) {
            TrailerNoticeStatus.DRAFT -> {
                when (next) {
                    TrailerNoticeStatus.ACTIVE -> return true
                }
                return false
            }
            TrailerNoticeStatus.PENDING -> {
                when (next) {
                    TrailerNoticeStatus.ACTIVE, TrailerNoticeStatus.CLOSED -> return true
                }
                return false
            }
            TrailerNoticeStatus.ACTIVE -> {
                when (next) {
                    TrailerNoticeStatus.SUSPENDED, TrailerNoticeStatus.CLOSED -> return true
                }
                return false
            }
            TrailerNoticeStatus.SUSPENDED -> {
                when (next) {
                    TrailerNoticeStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerNoticeStatus) {
        if (!canMoveTo(next)) {
            error("trailer notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = amount

    fun snapshot(): TrailerNotice {
        return TrailerNotice(
            code = code,
            name = name,
            dock = dock,
            locale = locale,
            source = source,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a trailer notice in status draft. */
fun newTrailerNotice(code: String, name: String): TrailerNotice {
    return TrailerNotice(
        code = code,
        name = name,
        dock = "",
        locale = "",
        source = "",
        amount = 0.0,
        status = TrailerNoticeStatus.DRAFT,
    )
}
