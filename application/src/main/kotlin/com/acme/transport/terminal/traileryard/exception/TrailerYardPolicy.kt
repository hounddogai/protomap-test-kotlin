package com.acme.transport.terminal.traileryard.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard policy is in its life cycle. */
enum class TrailerYardPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a trailer yard policy and its current status. */
data class TrailerYardPolicy(
    var code: String,
    var name: String,
    var loadKg: Double,
    var createdAt: Int,
    var notes: String,
    var shiftCode: String,
    var approved: Boolean,
    var lane: String,
    var visible: Boolean,
    var status: TrailerYardPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardPolicyStatus): Boolean {
        when (status) {
            TrailerYardPolicyStatus.CREATED -> return next == TrailerYardPolicyStatus.RETIRED
            TrailerYardPolicyStatus.VALIDATED -> return next == TrailerYardPolicyStatus.RETIRED
            TrailerYardPolicyStatus.PUBLISHED -> {
                when (next) {
                    TrailerYardPolicyStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerYardPolicyStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard policy $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"trailer yard policy"} $code"

    fun adjustedTotal(): Double = round((createdAt.toDouble() - loadKg) * 100) / 100

    fun snapshot(): TrailerYardPolicy {
        return TrailerYardPolicy(
            code = code,
            name = name,
            loadKg = loadKg,
            createdAt = createdAt,
            notes = notes,
            shiftCode = shiftCode,
            approved = approved,
            lane = lane,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a trailer yard policy in status created. */
fun newTrailerYardPolicy(code: String, name: String): TrailerYardPolicy {
    return TrailerYardPolicy(
        code = code,
        name = name,
        loadKg = 0.0,
        createdAt = 0,
        notes = "",
        shiftCode = "",
        approved = false,
        lane = "",
        visible = false,
        status = TrailerYardPolicyStatus.CREATED,
    )
}
