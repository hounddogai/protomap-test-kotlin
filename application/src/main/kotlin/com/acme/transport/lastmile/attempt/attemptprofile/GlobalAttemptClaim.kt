package com.acme.transport.lastmile.attempt.attemptprofile

import com.acme.transport.kit.text.normalize

/** Where a global attempt claim is in its life cycle. */
enum class GlobalAttemptClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A global attempt claim. Its code is unique within a tenant. */
data class GlobalAttemptClaim(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var fuelLiters: Double,
    var carrier: String,
    var status: GlobalAttemptClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAttemptClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAttemptClaimStatus): Boolean {
        when (status) {
            GlobalAttemptClaimStatus.NEW -> {
                when (next) {
                    GlobalAttemptClaimStatus.QUEUED, GlobalAttemptClaimStatus.DONE -> return true
                }
                return false
            }
            GlobalAttemptClaimStatus.QUEUED -> {
                when (next) {
                    GlobalAttemptClaimStatus.RUNNING, GlobalAttemptClaimStatus.DONE -> return true
                }
                return false
            }
            GlobalAttemptClaimStatus.RUNNING -> return next == GlobalAttemptClaimStatus.DONE || next == GlobalAttemptClaimStatus.FAILED
            GlobalAttemptClaimStatus.DONE -> return next == GlobalAttemptClaimStatus.FAILED
        }
        return false
    }

    fun moveTo(next: GlobalAttemptClaimStatus) {
        if (!canMoveTo(next)) {
            error("global attempt claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): GlobalAttemptClaim {
        return GlobalAttemptClaim(
            code = code,
            label = label,
            tags = tags,
            fuelLiters = fuelLiters,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global attempt claim in status new. */
fun newGlobalAttemptClaim(code: String, label: String): GlobalAttemptClaim {
    return GlobalAttemptClaim(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        fuelLiters = 0.0,
        carrier = "",
        status = GlobalAttemptClaimStatus.NEW,
    )
}
