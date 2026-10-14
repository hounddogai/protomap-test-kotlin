package com.acme.transport.network.node.score

import com.acme.transport.kit.text.normalize

// The rolling node score records of network's node work, and the rules, stores, and calculations around them.

/** Where a rolling node score is in its life cycle. */
enum class RollingNodeScoreStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A rolling node score. Its code is unique within a tenant. */
data class RollingNodeScore(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var locale: String,
    var currency: String,
    var phone: String,
    var timezone: String,
    var unit: String,
    var active: Boolean,
    var status: RollingNodeScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingNodeScoreStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingNodeScoreStatus): Boolean {
        when (status) {
            RollingNodeScoreStatus.REQUESTED -> return next == RollingNodeScoreStatus.EXPIRED
            RollingNodeScoreStatus.GRANTED -> return next == RollingNodeScoreStatus.EXPIRED
            RollingNodeScoreStatus.REVOKED -> return next == RollingNodeScoreStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RollingNodeScoreStatus) {
        if (!canMoveTo(next)) {
            error("rolling node score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling node score"} $code"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): RollingNodeScore {
        return RollingNodeScore(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            locale = locale,
            currency = currency,
            phone = phone,
            timezone = timezone,
            unit = unit,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rolling node score in status requested. */
fun newRollingNodeScore(code: String, title: String): RollingNodeScore {
    return RollingNodeScore(
        code = code,
        title = title,
        etaMinutes = 0,
        locale = "",
        currency = "",
        phone = "",
        timezone = "",
        unit = "",
        active = false,
        status = RollingNodeScoreStatus.REQUESTED,
    )
}
