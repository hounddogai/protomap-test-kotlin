package com.acme.transport.network.hub.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub request is in its life cycle. */
enum class HubRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A hub request. Its code is unique within a tenant. */
data class HubRequest(
    var code: String,
    var label: String,
    var category: String,
    var amount: Double,
    var score: Double,
    var locale: String,
    var expiresAt: Int,
    var summary: String,
    var status: HubRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: HubRequestStatus): Boolean {
        when (status) {
            HubRequestStatus.PROPOSED -> {
                when (next) {
                    HubRequestStatus.REJECTED, HubRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
            HubRequestStatus.APPROVED -> return next == HubRequestStatus.REJECTED || next == HubRequestStatus.WITHDRAWN
            HubRequestStatus.REJECTED -> {
                when (next) {
                    HubRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubRequestStatus) {
        if (!canMoveTo(next)) {
            error("hub request $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedTotal(): Double = round(score * expiresAt.toDouble() * 100) / 100

    fun snapshot(): HubRequest {
        return HubRequest(
            code = code,
            label = label,
            category = category,
            amount = amount,
            score = score,
            locale = locale,
            expiresAt = expiresAt,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a hub request in status proposed. */
fun newHubRequest(code: String, label: String): HubRequest {
    return HubRequest(
        code = code,
        label = label,
        category = "",
        amount = 0.0,
        score = 0.0,
        locale = "",
        expiresAt = 0,
        summary = "",
        status = HubRequestStatus.PROPOSED,
    )
}
