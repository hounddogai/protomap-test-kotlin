package com.acme.transport.linehaul.tender.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender claim is in its life cycle. */
enum class TenderClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a tender claim and its current status. */
data class TenderClaim(
    var code: String,
    var name: String,
    var ratio: Double,
    var capacity: Int,
    var lane: String,
    var maximum: Double,
    var status: TenderClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderClaimStatus): Boolean {
        when (status) {
            TenderClaimStatus.IDLE -> {
                when (next) {
                    TenderClaimStatus.WARMING -> return true
                }
                return false
            }
            TenderClaimStatus.WARMING -> return next == TenderClaimStatus.READY || next == TenderClaimStatus.STOPPED
            TenderClaimStatus.READY -> {
                when (next) {
                    TenderClaimStatus.DRAINING -> return true
                }
                return false
            }
            TenderClaimStatus.DRAINING -> return next == TenderClaimStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TenderClaimStatus) {
        if (!canMoveTo(next)) {
            error("tender claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tender claim"} $code"

    fun weightedExposure(): Double = round((capacity.toDouble() - ratio) * 100) / 100

    fun snapshot(): TenderClaim {
        return TenderClaim(
            code = code,
            name = name,
            ratio = ratio,
            capacity = capacity,
            lane = lane,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tender claim in status idle. */
fun newTenderClaim(code: String, name: String): TenderClaim {
    return TenderClaim(
        code = code,
        name = name,
        ratio = 0.0,
        capacity = 0,
        lane = "",
        maximum = 0.0,
        status = TenderClaimStatus.IDLE,
    )
}
