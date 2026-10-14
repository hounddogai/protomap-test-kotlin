package com.acme.transport.network.hub.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global hub policy records of network's hub work, and the rules, stores, and calculations around them.

/** Where a global hub policy is in its life cycle. */
enum class GlobalHubPolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global hub policy. Its code is unique within a tenant. */
data class GlobalHubPolicy(
    var code: String,
    var title: String,
    var graceDays: Int,
    var batchSize: Int,
    var checksum: String,
    var status: GlobalHubPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalHubPolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalHubPolicyStatus): Boolean {
        when (status) {
            GlobalHubPolicyStatus.PROPOSED -> return next == GlobalHubPolicyStatus.APPROVED
            GlobalHubPolicyStatus.APPROVED -> {
                when (next) {
                    GlobalHubPolicyStatus.REJECTED, GlobalHubPolicyStatus.WITHDRAWN -> return true
                }
                return false
            }
            GlobalHubPolicyStatus.REJECTED -> {
                when (next) {
                    GlobalHubPolicyStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalHubPolicyStatus) {
        if (!canMoveTo(next)) {
            error("global hub policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global hub policy"} $code"

    fun weightedExposure(): Double = round(graceDays.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): GlobalHubPolicy {
        return GlobalHubPolicy(
            code = code,
            title = title,
            graceDays = graceDays,
            batchSize = batchSize,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global hub policy in status proposed. */
fun newGlobalHubPolicy(code: String, title: String): GlobalHubPolicy {
    return GlobalHubPolicy(
        code = code,
        title = title,
        graceDays = 0,
        batchSize = 0,
        checksum = "",
        status = GlobalHubPolicyStatus.PROPOSED,
    )
}
