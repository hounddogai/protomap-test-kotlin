package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped backhaul variance records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a capped backhaul variance is in its life cycle. */
enum class CappedBackhaulVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a capped backhaul variance and its current status. */
data class CappedBackhaulVariance(
    var code: String,
    var title: String,
    var discount: Double,
    var floorValue: Double,
    var durationMinutes: Int,
    var name: String,
    var status: CappedBackhaulVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedBackhaulVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedBackhaulVarianceStatus): Boolean {
        when (status) {
            CappedBackhaulVarianceStatus.IDLE -> return next == CappedBackhaulVarianceStatus.WARMING || next == CappedBackhaulVarianceStatus.READY
            CappedBackhaulVarianceStatus.WARMING -> {
                when (next) {
                    CappedBackhaulVarianceStatus.READY, CappedBackhaulVarianceStatus.STOPPED -> return true
                }
                return false
            }
            CappedBackhaulVarianceStatus.READY -> return next == CappedBackhaulVarianceStatus.DRAINING
            CappedBackhaulVarianceStatus.DRAINING -> return next == CappedBackhaulVarianceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CappedBackhaulVarianceStatus) {
        if (!canMoveTo(next)) {
            error("capped backhaul variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedValue(): Double = round(durationMinutes.toDouble() * discount * 100) / 100

    fun snapshot(): CappedBackhaulVariance {
        return CappedBackhaulVariance(
            code = code,
            title = title,
            discount = discount,
            floorValue = floorValue,
            durationMinutes = durationMinutes,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped backhaul variance in status idle. */
fun newCappedBackhaulVariance(code: String, title: String): CappedBackhaulVariance {
    return CappedBackhaulVariance(
        code = code,
        title = title,
        discount = 0.0,
        floorValue = 0.0,
        durationMinutes = 0,
        name = "",
        status = CappedBackhaulVarianceStatus.IDLE,
    )
}
