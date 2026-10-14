package com.acme.transport.linehaul.relay.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal relay request is in its life cycle. */
enum class SeasonalRelayRequestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A seasonal relay request. Its code is unique within a tenant. */
data class SeasonalRelayRequest(
    var code: String,
    var label: String,
    var rate: Double,
    var channel: String,
    var sequence: Int,
    var status: SeasonalRelayRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRelayRequestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRelayRequestStatus): Boolean {
        when (status) {
            SeasonalRelayRequestStatus.DRAFT -> {
                when (next) {
                    SeasonalRelayRequestStatus.ACTIVE, SeasonalRelayRequestStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalRelayRequestStatus.PENDING -> {
                when (next) {
                    SeasonalRelayRequestStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalRelayRequestStatus.ACTIVE -> {
                when (next) {
                    SeasonalRelayRequestStatus.SUSPENDED -> return true
                }
                return false
            }
            SeasonalRelayRequestStatus.SUSPENDED -> return next == SeasonalRelayRequestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SeasonalRelayRequestStatus) {
        if (!canMoveTo(next)) {
            error("seasonal relay request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedExposure(): Double = round((rate - sequence.toDouble()) * 100) / 100

    fun snapshot(): SeasonalRelayRequest {
        return SeasonalRelayRequest(
            code = code,
            label = label,
            rate = rate,
            channel = channel,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a seasonal relay request in status draft. */
fun newSeasonalRelayRequest(code: String, label: String): SeasonalRelayRequest {
    return SeasonalRelayRequest(
        code = code,
        label = label,
        rate = 0.0,
        channel = "",
        sequence = 0,
        status = SeasonalRelayRequestStatus.DRAFT,
    )
}
