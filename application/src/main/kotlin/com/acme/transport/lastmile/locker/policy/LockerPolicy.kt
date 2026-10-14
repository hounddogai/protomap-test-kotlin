package com.acme.transport.lastmile.locker.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The locker policy records of lastmile's locker work, and the rules, stores, and calculations around them.

/** Where a locker policy is in its life cycle. */
enum class LockerPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A locker policy. Its code is unique within a tenant. */
data class LockerPolicy(
    var code: String,
    var title: String,
    var quantity: Int,
    var startsAt: Int,
    var etaMinutes: Int,
    var discount: Double,
    var status: LockerPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerPolicyStatus): Boolean {
        when (status) {
            LockerPolicyStatus.CREATED -> return next == LockerPolicyStatus.VALIDATED
            LockerPolicyStatus.VALIDATED -> {
                when (next) {
                    LockerPolicyStatus.PUBLISHED -> return true
                }
                return false
            }
            LockerPolicyStatus.PUBLISHED -> {
                when (next) {
                    LockerPolicyStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerPolicyStatus) {
        if (!canMoveTo(next)) {
            error("locker policy $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"locker policy"} $code"

    fun effectiveVolume(): Double = round((etaMinutes.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): LockerPolicy {
        return LockerPolicy(
            code = code,
            title = title,
            quantity = quantity,
            startsAt = startsAt,
            etaMinutes = etaMinutes,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a locker policy in status created. */
fun newLockerPolicy(code: String, title: String): LockerPolicy {
    return LockerPolicy(
        code = code,
        title = title,
        quantity = 0,
        startsAt = 0,
        etaMinutes = 0,
        discount = 0.0,
        status = LockerPolicyStatus.CREATED,
    )
}
