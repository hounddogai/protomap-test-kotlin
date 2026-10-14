package com.acme.transport.network.zone.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped zone target is in its life cycle. */
enum class CappedZoneTargetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a capped zone target and its current status. */
data class CappedZoneTarget(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var description: String,
    var etaMinutes: Int,
    var phone: String,
    var dock: String,
    var status: CappedZoneTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedZoneTargetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedZoneTargetStatus): Boolean {
        when (status) {
            CappedZoneTargetStatus.OPEN -> return next == CappedZoneTargetStatus.IN_REVIEW || next == CappedZoneTargetStatus.ARCHIVED
            CappedZoneTargetStatus.IN_REVIEW -> {
                when (next) {
                    CappedZoneTargetStatus.RESOLVED -> return true
                }
                return false
            }
            CappedZoneTargetStatus.RESOLVED -> {
                when (next) {
                    CappedZoneTargetStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedZoneTargetStatus.REOPENED -> return next == CappedZoneTargetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CappedZoneTargetStatus) {
        if (!canMoveTo(next)) {
            error("capped zone target $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedTotal(): Double = round((expiresAt.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): CappedZoneTarget {
        return CappedZoneTarget(
            code = code,
            name = name,
            expiresAt = expiresAt,
            description = description,
            etaMinutes = etaMinutes,
            phone = phone,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped zone target in status open. */
fun newCappedZoneTarget(code: String, name: String): CappedZoneTarget {
    return CappedZoneTarget(
        code = code,
        name = name,
        expiresAt = 0,
        description = "",
        etaMinutes = 0,
        phone = "",
        dock = "",
        status = CappedZoneTargetStatus.OPEN,
    )
}
