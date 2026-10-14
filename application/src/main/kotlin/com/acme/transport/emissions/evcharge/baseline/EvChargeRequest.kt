package com.acme.transport.emissions.evcharge.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge request is in its life cycle. */
enum class EvChargeRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A ev charge request as the ev charge team keeps it. */
data class EvChargeRequest(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var channel: String,
    var score: Double,
    var amount: Double,
    var externalRef: String,
    var capacity: Int,
    var limit: Int,
    var batchSize: Int,
    var budget: Double,
    var status: EvChargeRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeRequestStatus): Boolean {
        when (status) {
            EvChargeRequestStatus.REQUESTED -> return next == EvChargeRequestStatus.EXPIRED
            EvChargeRequestStatus.GRANTED -> return next == EvChargeRequestStatus.REVOKED || next == EvChargeRequestStatus.EXPIRED
            EvChargeRequestStatus.REVOKED -> return next == EvChargeRequestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: EvChargeRequestStatus) {
        if (!canMoveTo(next)) {
            error("ev charge request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"ev charge request"} $code"

    fun effectiveVolume(): Double = round((batchSize.toDouble() - score) * 100) / 100

    fun snapshot(): EvChargeRequest {
        return EvChargeRequest(
            code = code,
            name = name,
            pinned = pinned,
            channel = channel,
            score = score,
            amount = amount,
            externalRef = externalRef,
            capacity = capacity,
            limit = limit,
            batchSize = batchSize,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a ev charge request in status requested. */
fun newEvChargeRequest(code: String, name: String): EvChargeRequest {
    return EvChargeRequest(
        code = code,
        name = name,
        pinned = false,
        channel = "",
        score = 0.0,
        amount = 0.0,
        externalRef = "",
        capacity = 0,
        limit = 0,
        batchSize = 0,
        budget = 0.0,
        status = EvChargeRequestStatus.REQUESTED,
    )
}
