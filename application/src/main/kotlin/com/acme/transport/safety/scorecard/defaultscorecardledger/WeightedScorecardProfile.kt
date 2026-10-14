package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted scorecard profile is in its life cycle. */
enum class WeightedScorecardProfileStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One weighted scorecard profile, with what the planners need to act on it. */
data class WeightedScorecardProfile(
    var code: String,
    var label: String,
    var unit: String,
    var quantity: Int,
    var retries: Int,
    var weight: Double,
    var region: String,
    var etaMinutes: Int,
    var margin: Double,
    var status: WeightedScorecardProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedScorecardProfileStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedScorecardProfileStatus): Boolean {
        when (status) {
            WeightedScorecardProfileStatus.NEW -> {
                when (next) {
                    WeightedScorecardProfileStatus.DONE, WeightedScorecardProfileStatus.FAILED -> return true
                }
                return false
            }
            WeightedScorecardProfileStatus.QUEUED -> return next == WeightedScorecardProfileStatus.RUNNING || next == WeightedScorecardProfileStatus.DONE
            WeightedScorecardProfileStatus.RUNNING -> {
                when (next) {
                    WeightedScorecardProfileStatus.FAILED -> return true
                }
                return false
            }
            WeightedScorecardProfileStatus.DONE -> {
                when (next) {
                    WeightedScorecardProfileStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedScorecardProfileStatus) {
        if (!canMoveTo(next)) {
            error("weighted scorecard profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedExposure(): Double = round((quantity.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): WeightedScorecardProfile {
        return WeightedScorecardProfile(
            code = code,
            label = label,
            unit = unit,
            quantity = quantity,
            retries = retries,
            weight = weight,
            region = region,
            etaMinutes = etaMinutes,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a weighted scorecard profile in status new. */
fun newWeightedScorecardProfile(code: String, label: String): WeightedScorecardProfile {
    return WeightedScorecardProfile(
        code = code,
        label = label,
        unit = "",
        quantity = 0,
        retries = 0,
        weight = 0.0,
        region = "",
        etaMinutes = 0,
        margin = 0.0,
        status = WeightedScorecardProfileStatus.NEW,
    )
}
