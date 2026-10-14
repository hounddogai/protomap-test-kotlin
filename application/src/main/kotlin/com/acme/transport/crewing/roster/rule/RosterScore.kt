package com.acme.transport.crewing.roster.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster score is in its life cycle. */
enum class RosterScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A roster score as the roster team keeps it. */
data class RosterScore(
    var code: String,
    var label: String,
    var shiftCode: String,
    var amount: Double,
    var name: String,
    var threshold: Double,
    var endsAt: Int,
    var discount: Double,
    var loadKg: Double,
    var rate: Double,
    var stopCount: Int,
    var status: RosterScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterScoreStatus): Boolean {
        when (status) {
            RosterScoreStatus.NEW -> {
                when (next) {
                    RosterScoreStatus.DONE -> return true
                }
                return false
            }
            RosterScoreStatus.QUEUED -> return next == RosterScoreStatus.RUNNING || next == RosterScoreStatus.DONE
            RosterScoreStatus.RUNNING -> return next == RosterScoreStatus.DONE
            RosterScoreStatus.DONE -> return next == RosterScoreStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RosterScoreStatus) {
        if (!canMoveTo(next)) {
            error("roster score $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedLoad(): Double = round((rate - amount) * 100) / 100

    fun snapshot(): RosterScore {
        return RosterScore(
            code = code,
            label = label,
            shiftCode = shiftCode,
            amount = amount,
            name = name,
            threshold = threshold,
            endsAt = endsAt,
            discount = discount,
            loadKg = loadKg,
            rate = rate,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a roster score in status new. */
fun newRosterScore(code: String, label: String): RosterScore {
    return RosterScore(
        code = code,
        label = label,
        shiftCode = "",
        amount = 0.0,
        name = "",
        threshold = 0.0,
        endsAt = 0,
        discount = 0.0,
        loadKg = 0.0,
        rate = 0.0,
        stopCount = 0,
        status = RosterScoreStatus.NEW,
    )
}
