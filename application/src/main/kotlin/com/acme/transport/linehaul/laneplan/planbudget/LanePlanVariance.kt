package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan variance is in its life cycle. */
enum class LanePlanVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One lane plan variance, with what the planners need to act on it. */
data class LanePlanVariance(
    var code: String,
    var title: String,
    var phone: String,
    var weight: Double,
    var approved: Boolean,
    var pinned: Boolean,
    var etaMinutes: Int,
    var locale: String,
    var depot: String,
    var externalRef: String,
    var status: LanePlanVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanVarianceStatus): Boolean {
        when (status) {
            LanePlanVarianceStatus.IDLE -> {
                when (next) {
                    LanePlanVarianceStatus.DRAINING -> return true
                }
                return false
            }
            LanePlanVarianceStatus.WARMING -> {
                when (next) {
                    LanePlanVarianceStatus.READY, LanePlanVarianceStatus.DRAINING -> return true
                }
                return false
            }
            LanePlanVarianceStatus.READY -> {
                when (next) {
                    LanePlanVarianceStatus.DRAINING, LanePlanVarianceStatus.STOPPED -> return true
                }
                return false
            }
            LanePlanVarianceStatus.DRAINING -> return next == LanePlanVarianceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LanePlanVarianceStatus) {
        if (!canMoveTo(next)) {
            error("lane plan variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lane plan variance"} $code"

    fun netValue(): Double = round((weight + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): LanePlanVariance {
        return LanePlanVariance(
            code = code,
            title = title,
            phone = phone,
            weight = weight,
            approved = approved,
            pinned = pinned,
            etaMinutes = etaMinutes,
            locale = locale,
            depot = depot,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a lane plan variance in status idle. */
fun newLanePlanVariance(code: String, title: String): LanePlanVariance {
    return LanePlanVariance(
        code = code,
        title = title,
        phone = "",
        weight = 0.0,
        approved = false,
        pinned = false,
        etaMinutes = 0,
        locale = "",
        depot = "",
        externalRef = "",
        status = LanePlanVarianceStatus.IDLE,
    )
}
