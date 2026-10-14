package com.acme.transport.crewing.restbreak.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break score is in its life cycle. */
enum class RestBreakScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rest break score as the rest break team keeps it. */
data class RestBreakScore(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var shiftCode: String,
    var label: String,
    var phone: String,
    var batchSize: Int,
    var weight: Double,
    var status: RestBreakScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakScoreStatus): Boolean {
        when (status) {
            RestBreakScoreStatus.NEW -> return next == RestBreakScoreStatus.QUEUED || next == RestBreakScoreStatus.FAILED
            RestBreakScoreStatus.QUEUED -> return next == RestBreakScoreStatus.DONE || next == RestBreakScoreStatus.FAILED
            RestBreakScoreStatus.RUNNING -> return next == RestBreakScoreStatus.DONE || next == RestBreakScoreStatus.FAILED
            RestBreakScoreStatus.DONE -> return next == RestBreakScoreStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RestBreakScoreStatus) {
        if (!canMoveTo(next)) {
            error("rest break score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rest break score"} $code"

    fun weightedValue(): Double = round((etaMinutes.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): RestBreakScore {
        return RestBreakScore(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            shiftCode = shiftCode,
            label = label,
            phone = phone,
            batchSize = batchSize,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rest break score in status new. */
fun newRestBreakScore(code: String, name: String): RestBreakScore {
    return RestBreakScore(
        code = code,
        name = name,
        etaMinutes = 0,
        shiftCode = "",
        label = "",
        phone = "",
        batchSize = 0,
        weight = 0.0,
        status = RestBreakScoreStatus.NEW,
    )
}
