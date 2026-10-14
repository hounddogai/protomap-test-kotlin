package com.acme.transport.lastmile.locker.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default locker digest is in its life cycle. */
enum class DefaultLockerDigestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A default locker digest as the locker team keeps it. */
data class DefaultLockerDigest(
    var code: String,
    var name: String,
    var label: String,
    var endsAt: Int,
    var ceiling: Double,
    var locale: String,
    var pinned: Boolean,
    var etaMinutes: Int,
    var minimum: Double,
    var shiftCode: String,
    var status: DefaultLockerDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultLockerDigestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultLockerDigestStatus): Boolean {
        when (status) {
            DefaultLockerDigestStatus.PROPOSED -> {
                when (next) {
                    DefaultLockerDigestStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefaultLockerDigestStatus.APPROVED -> {
                when (next) {
                    DefaultLockerDigestStatus.REJECTED, DefaultLockerDigestStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefaultLockerDigestStatus.REJECTED -> return next == DefaultLockerDigestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DefaultLockerDigestStatus) {
        if (!canMoveTo(next)) {
            error("default locker digest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($name)"

    fun projectedExposure(): Double = round(minimum * endsAt.toDouble() * 100) / 100

    fun snapshot(): DefaultLockerDigest {
        return DefaultLockerDigest(
            code = code,
            name = name,
            label = label,
            endsAt = endsAt,
            ceiling = ceiling,
            locale = locale,
            pinned = pinned,
            etaMinutes = etaMinutes,
            minimum = minimum,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a default locker digest in status proposed. */
fun newDefaultLockerDigest(code: String, name: String): DefaultLockerDigest {
    return DefaultLockerDigest(
        code = code,
        name = name,
        label = "",
        endsAt = 0,
        ceiling = 0.0,
        locale = "",
        pinned = false,
        etaMinutes = 0,
        minimum = 0.0,
        shiftCode = "",
        status = DefaultLockerDigestStatus.PROPOSED,
    )
}
