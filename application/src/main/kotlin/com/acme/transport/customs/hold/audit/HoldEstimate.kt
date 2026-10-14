package com.acme.transport.customs.hold.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold estimate is in its life cycle. */
enum class HoldEstimateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a hold estimate and its current status. */
data class HoldEstimate(
    var code: String,
    var name: String,
    var score: Double,
    var revision: Int,
    var etaMinutes: Int,
    var cost: Double,
    var endsAt: Int,
    var status: HoldEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldEstimateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldEstimateStatus): Boolean {
        when (status) {
            HoldEstimateStatus.REQUESTED -> return next == HoldEstimateStatus.GRANTED || next == HoldEstimateStatus.REVOKED
            HoldEstimateStatus.GRANTED -> {
                when (next) {
                    HoldEstimateStatus.REVOKED -> return true
                }
                return false
            }
            HoldEstimateStatus.REVOKED -> return next == HoldEstimateStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: HoldEstimateStatus) {
        if (!canMoveTo(next)) {
            error("hold estimate $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun weightedVolume(): Double = round(etaMinutes.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): HoldEstimate {
        return HoldEstimate(
            code = code,
            name = name,
            score = score,
            revision = revision,
            etaMinutes = etaMinutes,
            cost = cost,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a hold estimate in status requested. */
fun newHoldEstimate(code: String, name: String): HoldEstimate {
    return HoldEstimate(
        code = code,
        name = name,
        score = 0.0,
        revision = 0,
        etaMinutes = 0,
        cost = 0.0,
        endsAt = 0,
        status = HoldEstimateStatus.REQUESTED,
    )
}
