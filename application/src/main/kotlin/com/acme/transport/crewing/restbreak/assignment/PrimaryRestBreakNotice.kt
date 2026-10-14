package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary rest break notice is in its life cycle. */
enum class PrimaryRestBreakNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A primary rest break notice. Its code is unique within a tenant. */
data class PrimaryRestBreakNotice(
    var code: String,
    var name: String,
    var revision: Int,
    var createdAt: Int,
    var odometer: Int,
    var status: PrimaryRestBreakNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRestBreakNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRestBreakNoticeStatus): Boolean {
        when (status) {
            PrimaryRestBreakNoticeStatus.PROPOSED -> {
                when (next) {
                    PrimaryRestBreakNoticeStatus.REJECTED -> return true
                }
                return false
            }
            PrimaryRestBreakNoticeStatus.APPROVED -> return next == PrimaryRestBreakNoticeStatus.REJECTED || next == PrimaryRestBreakNoticeStatus.WITHDRAWN
            PrimaryRestBreakNoticeStatus.REJECTED -> {
                when (next) {
                    PrimaryRestBreakNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryRestBreakNoticeStatus) {
        if (!canMoveTo(next)) {
            error("primary rest break notice $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun effectiveTotal(): Double = round((revision.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): PrimaryRestBreakNotice {
        return PrimaryRestBreakNotice(
            code = code,
            name = name,
            revision = revision,
            createdAt = createdAt,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary rest break notice in status proposed. */
fun newPrimaryRestBreakNotice(code: String, name: String): PrimaryRestBreakNotice {
    return PrimaryRestBreakNotice(
        code = code,
        name = name,
        revision = 0,
        createdAt = 0,
        odometer = 0,
        status = PrimaryRestBreakNoticeStatus.PROPOSED,
    )
}
