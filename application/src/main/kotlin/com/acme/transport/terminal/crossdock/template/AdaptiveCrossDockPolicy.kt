package com.acme.transport.terminal.crossdock.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive cross dock policy is in its life cycle. */
enum class AdaptiveCrossDockPolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a adaptive cross dock policy and its current status. */
data class AdaptiveCrossDockPolicy(
    var code: String,
    var name: String,
    var ratio: Double,
    var quantity: Int,
    var loadKg: Double,
    var archived: Boolean,
    var status: AdaptiveCrossDockPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCrossDockPolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCrossDockPolicyStatus): Boolean {
        when (status) {
            AdaptiveCrossDockPolicyStatus.PROPOSED -> return next == AdaptiveCrossDockPolicyStatus.APPROVED || next == AdaptiveCrossDockPolicyStatus.WITHDRAWN
            AdaptiveCrossDockPolicyStatus.APPROVED -> return next == AdaptiveCrossDockPolicyStatus.REJECTED || next == AdaptiveCrossDockPolicyStatus.WITHDRAWN
            AdaptiveCrossDockPolicyStatus.REJECTED -> return next == AdaptiveCrossDockPolicyStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AdaptiveCrossDockPolicyStatus) {
        if (!canMoveTo(next)) {
            error("adaptive cross dock policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedExposure(): Double = round((ratio + quantity.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveCrossDockPolicy {
        return AdaptiveCrossDockPolicy(
            code = code,
            name = name,
            ratio = ratio,
            quantity = quantity,
            loadKg = loadKg,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive cross dock policy in status proposed. */
fun newAdaptiveCrossDockPolicy(code: String, name: String): AdaptiveCrossDockPolicy {
    return AdaptiveCrossDockPolicy(
        code = code,
        name = name,
        ratio = 0.0,
        quantity = 0,
        loadKg = 0.0,
        archived = false,
        status = AdaptiveCrossDockPolicyStatus.PROPOSED,
    )
}
