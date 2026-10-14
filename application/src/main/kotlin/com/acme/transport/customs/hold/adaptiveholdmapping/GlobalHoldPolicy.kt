package com.acme.transport.customs.hold.adaptiveholdmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global hold policy is in its life cycle. */
enum class GlobalHoldPolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global hold policy as the hold team keeps it. */
data class GlobalHoldPolicy(
    var code: String,
    var name: String,
    var floorValue: Double,
    var depot: String,
    var graceDays: Int,
    var notes: String,
    var checksum: String,
    var version: Int,
    var status: GlobalHoldPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalHoldPolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalHoldPolicyStatus): Boolean {
        when (status) {
            GlobalHoldPolicyStatus.PROPOSED -> {
                when (next) {
                    GlobalHoldPolicyStatus.REJECTED -> return true
                }
                return false
            }
            GlobalHoldPolicyStatus.APPROVED -> return next == GlobalHoldPolicyStatus.WITHDRAWN
            GlobalHoldPolicyStatus.REJECTED -> return next == GlobalHoldPolicyStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalHoldPolicyStatus) {
        if (!canMoveTo(next)) {
            error("global hold policy $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"global hold policy"} $code"

    fun effectiveExposure(): Double = round((version.toDouble() - floorValue) * 100) / 100

    fun snapshot(): GlobalHoldPolicy {
        return GlobalHoldPolicy(
            code = code,
            name = name,
            floorValue = floorValue,
            depot = depot,
            graceDays = graceDays,
            notes = notes,
            checksum = checksum,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a global hold policy in status proposed. */
fun newGlobalHoldPolicy(code: String, name: String): GlobalHoldPolicy {
    return GlobalHoldPolicy(
        code = code,
        name = name,
        floorValue = 0.0,
        depot = "",
        graceDays = 0,
        notes = "",
        checksum = "",
        version = 0,
        status = GlobalHoldPolicyStatus.PROPOSED,
    )
}
