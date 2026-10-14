package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary capacity score is in its life cycle. */
enum class PrimaryCapacityScoreStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a primary capacity score and its current status. */
data class PrimaryCapacityScore(
    var code: String,
    var name: String,
    var cost: Double,
    var sequence: Int,
    var shiftCode: String,
    var expiresAt: Int,
    var country: String,
    var status: PrimaryCapacityScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCapacityScoreStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCapacityScoreStatus): Boolean {
        when (status) {
            PrimaryCapacityScoreStatus.REQUESTED -> return next == PrimaryCapacityScoreStatus.GRANTED || next == PrimaryCapacityScoreStatus.REVOKED
            PrimaryCapacityScoreStatus.GRANTED -> {
                when (next) {
                    PrimaryCapacityScoreStatus.REVOKED -> return true
                }
                return false
            }
            PrimaryCapacityScoreStatus.REVOKED -> return next == PrimaryCapacityScoreStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PrimaryCapacityScoreStatus) {
        if (!canMoveTo(next)) {
            error("primary capacity score $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun weightedValue(): Double = round((expiresAt.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): PrimaryCapacityScore {
        return PrimaryCapacityScore(
            code = code,
            name = name,
            cost = cost,
            sequence = sequence,
            shiftCode = shiftCode,
            expiresAt = expiresAt,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a primary capacity score in status requested. */
fun newPrimaryCapacityScore(code: String, name: String): PrimaryCapacityScore {
    return PrimaryCapacityScore(
        code = code,
        name = name,
        cost = 0.0,
        sequence = 0,
        shiftCode = "",
        expiresAt = 0,
        country = "",
        status = PrimaryCapacityScoreStatus.REQUESTED,
    )
}
