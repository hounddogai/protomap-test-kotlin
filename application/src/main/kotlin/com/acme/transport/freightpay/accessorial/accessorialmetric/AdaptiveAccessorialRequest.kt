package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive accessorial request is in its life cycle. */
enum class AdaptiveAccessorialRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a adaptive accessorial request and its current status. */
data class AdaptiveAccessorialRequest(
    var code: String,
    var label: String,
    var attempts: Int,
    var score: Double,
    var summary: String,
    var ratio: Double,
    var maximum: Double,
    var status: AdaptiveAccessorialRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAccessorialRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAccessorialRequestStatus): Boolean {
        when (status) {
            AdaptiveAccessorialRequestStatus.REQUESTED -> {
                when (next) {
                    AdaptiveAccessorialRequestStatus.REVOKED -> return true
                }
                return false
            }
            AdaptiveAccessorialRequestStatus.GRANTED -> {
                when (next) {
                    AdaptiveAccessorialRequestStatus.REVOKED, AdaptiveAccessorialRequestStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveAccessorialRequestStatus.REVOKED -> {
                when (next) {
                    AdaptiveAccessorialRequestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveAccessorialRequestStatus) {
        if (!canMoveTo(next)) {
            error("adaptive accessorial request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive accessorial request"} $code"

    fun netExposure(): Double = round((maximum - ratio) * 100) / 100

    fun snapshot(): AdaptiveAccessorialRequest {
        return AdaptiveAccessorialRequest(
            code = code,
            label = label,
            attempts = attempts,
            score = score,
            summary = summary,
            ratio = ratio,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive accessorial request in status requested. */
fun newAdaptiveAccessorialRequest(code: String, label: String): AdaptiveAccessorialRequest {
    return AdaptiveAccessorialRequest(
        code = code,
        label = label,
        attempts = 0,
        score = 0.0,
        summary = "",
        ratio = 0.0,
        maximum = 0.0,
        status = AdaptiveAccessorialRequestStatus.REQUESTED,
    )
}
