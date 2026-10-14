package com.acme.transport.terminal.dwell.digest

import com.acme.transport.kit.text.normalize

/** Where a fallback dwell score is in its life cycle. */
enum class FallbackDwellScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a fallback dwell score and its current status. */
data class FallbackDwellScore(
    var code: String,
    var name: String,
    var shiftCode: String,
    var title: String,
    var depot: String,
    var vehicleId: String,
    var sequence: Int,
    var source: String,
    var ownerId: String,
    var status: FallbackDwellScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDwellScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDwellScoreStatus): Boolean {
        when (status) {
            FallbackDwellScoreStatus.NEW -> {
                when (next) {
                    FallbackDwellScoreStatus.RUNNING, FallbackDwellScoreStatus.FAILED -> return true
                }
                return false
            }
            FallbackDwellScoreStatus.QUEUED -> {
                when (next) {
                    FallbackDwellScoreStatus.DONE -> return true
                }
                return false
            }
            FallbackDwellScoreStatus.RUNNING -> return next == FallbackDwellScoreStatus.DONE || next == FallbackDwellScoreStatus.FAILED
            FallbackDwellScoreStatus.DONE -> {
                when (next) {
                    FallbackDwellScoreStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackDwellScoreStatus) {
        if (!canMoveTo(next)) {
            error("fallback dwell score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): FallbackDwellScore {
        return FallbackDwellScore(
            code = code,
            name = name,
            shiftCode = shiftCode,
            title = title,
            depot = depot,
            vehicleId = vehicleId,
            sequence = sequence,
            source = source,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fallback dwell score in status new. */
fun newFallbackDwellScore(code: String, name: String): FallbackDwellScore {
    return FallbackDwellScore(
        code = code,
        name = name,
        shiftCode = "",
        title = "",
        depot = "",
        vehicleId = "",
        sequence = 0,
        source = "",
        ownerId = "",
        status = FallbackDwellScoreStatus.NEW,
    )
}
