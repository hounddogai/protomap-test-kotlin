package com.acme.transport.freightpay.accessorial.accessorialpolicy

import com.acme.transport.kit.text.normalize

// The capped accessorial policy records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a capped accessorial policy is in its life cycle. */
enum class CappedAccessorialPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped accessorial policy as the accessorial team keeps it. */
data class CappedAccessorialPolicy(
    var code: String,
    var name: String,
    var description: String,
    var dock: String,
    var attempts: Int,
    var status: CappedAccessorialPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedAccessorialPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedAccessorialPolicyStatus): Boolean {
        when (status) {
            CappedAccessorialPolicyStatus.REQUESTED -> {
                when (next) {
                    CappedAccessorialPolicyStatus.GRANTED, CappedAccessorialPolicyStatus.EXPIRED -> return true
                }
                return false
            }
            CappedAccessorialPolicyStatus.GRANTED -> {
                when (next) {
                    CappedAccessorialPolicyStatus.REVOKED, CappedAccessorialPolicyStatus.EXPIRED -> return true
                }
                return false
            }
            CappedAccessorialPolicyStatus.REVOKED -> return next == CappedAccessorialPolicyStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedAccessorialPolicyStatus) {
        if (!canMoveTo(next)) {
            error("capped accessorial policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capped accessorial policy"} $code"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): CappedAccessorialPolicy {
        return CappedAccessorialPolicy(
            code = code,
            name = name,
            description = description,
            dock = dock,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped accessorial policy in status requested. */
fun newCappedAccessorialPolicy(code: String, name: String): CappedAccessorialPolicy {
    return CappedAccessorialPolicy(
        code = code,
        name = name,
        description = "",
        dock = "",
        attempts = 0,
        status = CappedAccessorialPolicyStatus.REQUESTED,
    )
}
