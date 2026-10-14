package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial score is in its life cycle. */
enum class AccessorialScoreStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A accessorial score. Its code is unique within a tenant. */
data class AccessorialScore(
    var code: String,
    var label: String,
    var floorValue: Double,
    var source: String,
    var score: Double,
    var etaMinutes: Int,
    var status: AccessorialScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialScoreStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialScoreStatus): Boolean {
        when (status) {
            AccessorialScoreStatus.REQUESTED -> return next == AccessorialScoreStatus.GRANTED || next == AccessorialScoreStatus.REVOKED
            AccessorialScoreStatus.GRANTED -> {
                when (next) {
                    AccessorialScoreStatus.REVOKED -> return true
                }
                return false
            }
            AccessorialScoreStatus.REVOKED -> {
                when (next) {
                    AccessorialScoreStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialScoreStatus) {
        if (!canMoveTo(next)) {
            error("accessorial score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"accessorial score"} $code"

    fun effectiveValue(): Double = round(floorValue * score * 100) / 100

    fun snapshot(): AccessorialScore {
        return AccessorialScore(
            code = code,
            label = label,
            floorValue = floorValue,
            source = source,
            score = score,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a accessorial score in status requested. */
fun newAccessorialScore(code: String, label: String): AccessorialScore {
    return AccessorialScore(
        code = code,
        label = label,
        floorValue = 0.0,
        source = "",
        score = 0.0,
        etaMinutes = 0,
        status = AccessorialScoreStatus.REQUESTED,
    )
}
