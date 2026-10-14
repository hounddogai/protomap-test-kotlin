package com.acme.transport.network.transittime.notice

import com.acme.transport.kit.text.normalize

// The tiered transit time notice records of network's transit time work, and the rules, stores, and
// calculations around them.

/** Where a tiered transit time notice is in its life cycle. */
enum class TieredTransitTimeNoticeStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A tiered transit time notice as the transit time team keeps it. */
data class TieredTransitTimeNotice(
    var code: String,
    var title: String,
    var ownerId: String,
    var category: String,
    var createdAt: Int,
    var status: TieredTransitTimeNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTransitTimeNoticeStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTransitTimeNoticeStatus): Boolean {
        when (status) {
            TieredTransitTimeNoticeStatus.DRAFT -> {
                when (next) {
                    TieredTransitTimeNoticeStatus.ACTIVE, TieredTransitTimeNoticeStatus.SUSPENDED -> return true
                }
                return false
            }
            TieredTransitTimeNoticeStatus.PENDING -> return next == TieredTransitTimeNoticeStatus.ACTIVE || next == TieredTransitTimeNoticeStatus.SUSPENDED
            TieredTransitTimeNoticeStatus.ACTIVE -> return next == TieredTransitTimeNoticeStatus.CLOSED
            TieredTransitTimeNoticeStatus.SUSPENDED -> {
                when (next) {
                    TieredTransitTimeNoticeStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredTransitTimeNoticeStatus) {
        if (!canMoveTo(next)) {
            error("tiered transit time notice $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): TieredTransitTimeNotice {
        return TieredTransitTimeNotice(
            code = code,
            title = title,
            ownerId = ownerId,
            category = category,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tiered transit time notice in status draft. */
fun newTieredTransitTimeNotice(code: String, title: String): TieredTransitTimeNotice {
    return TieredTransitTimeNotice(
        code = code,
        title = title,
        ownerId = "",
        category = "",
        createdAt = 0,
        status = TieredTransitTimeNoticeStatus.DRAFT,
    )
}
