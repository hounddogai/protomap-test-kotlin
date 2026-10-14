package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global relay variance is in its life cycle. */
enum class GlobalRelayVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global relay variance as the relay team keeps it. */
data class GlobalRelayVariance(
    var code: String,
    var label: String,
    var cost: Double,
    var score: Double,
    var phone: String,
    var threshold: Double,
    var version: Int,
    var notes: String,
    var status: GlobalRelayVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRelayVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRelayVarianceStatus): Boolean {
        when (status) {
            GlobalRelayVarianceStatus.PROPOSED -> return next == GlobalRelayVarianceStatus.APPROVED
            GlobalRelayVarianceStatus.APPROVED -> return next == GlobalRelayVarianceStatus.REJECTED
            GlobalRelayVarianceStatus.REJECTED -> return next == GlobalRelayVarianceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalRelayVarianceStatus) {
        if (!canMoveTo(next)) {
            error("global relay variance $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"global relay variance"} $code"

    fun projectedLoad(): Double = round((cost - version.toDouble()) * 100) / 100

    fun snapshot(): GlobalRelayVariance {
        return GlobalRelayVariance(
            code = code,
            label = label,
            cost = cost,
            score = score,
            phone = phone,
            threshold = threshold,
            version = version,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global relay variance in status proposed. */
fun newGlobalRelayVariance(code: String, label: String): GlobalRelayVariance {
    return GlobalRelayVariance(
        code = code,
        label = label,
        cost = 0.0,
        score = 0.0,
        phone = "",
        threshold = 0.0,
        version = 0,
        notes = "",
        status = GlobalRelayVarianceStatus.PROPOSED,
    )
}
