package com.acme.transport.network.servicearea.weightedareaclaim

import com.acme.transport.kit.text.normalize

/** Where a lenient service area score is in its life cycle. */
enum class LenientServiceAreaScoreStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A lenient service area score. Its code is unique within a tenant. */
data class LenientServiceAreaScore(
    var code: String,
    var title: String,
    var lane: String,
    var locale: String,
    var driverId: String,
    var ownerId: String,
    var quantity: Int,
    var status: LenientServiceAreaScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientServiceAreaScoreStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientServiceAreaScoreStatus): Boolean {
        when (status) {
            LenientServiceAreaScoreStatus.IDLE -> {
                when (next) {
                    LenientServiceAreaScoreStatus.WARMING -> return true
                }
                return false
            }
            LenientServiceAreaScoreStatus.WARMING -> return next == LenientServiceAreaScoreStatus.STOPPED
            LenientServiceAreaScoreStatus.READY -> return next == LenientServiceAreaScoreStatus.DRAINING || next == LenientServiceAreaScoreStatus.STOPPED
            LenientServiceAreaScoreStatus.DRAINING -> {
                when (next) {
                    LenientServiceAreaScoreStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientServiceAreaScoreStatus) {
        if (!canMoveTo(next)) {
            error("lenient service area score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): LenientServiceAreaScore {
        return LenientServiceAreaScore(
            code = code,
            title = title,
            lane = lane,
            locale = locale,
            driverId = driverId,
            ownerId = ownerId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a lenient service area score in status idle. */
fun newLenientServiceAreaScore(code: String, title: String): LenientServiceAreaScore {
    return LenientServiceAreaScore(
        code = code,
        title = title,
        lane = "",
        locale = "",
        driverId = "",
        ownerId = "",
        quantity = 0,
        status = LenientServiceAreaScoreStatus.IDLE,
    )
}
