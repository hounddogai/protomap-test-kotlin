package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling trailer yard policy is in its life cycle. */
enum class RollingTrailerYardPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a rolling trailer yard policy and its current status. */
data class RollingTrailerYardPolicy(
    var code: String,
    var title: String,
    var name: String,
    var amount: Double,
    var ceiling: Double,
    var checksum: String,
    var status: RollingTrailerYardPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTrailerYardPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTrailerYardPolicyStatus): Boolean {
        when (status) {
            RollingTrailerYardPolicyStatus.CREATED -> return next == RollingTrailerYardPolicyStatus.VALIDATED || next == RollingTrailerYardPolicyStatus.PUBLISHED
            RollingTrailerYardPolicyStatus.VALIDATED -> return next == RollingTrailerYardPolicyStatus.RETIRED
            RollingTrailerYardPolicyStatus.PUBLISHED -> return next == RollingTrailerYardPolicyStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RollingTrailerYardPolicyStatus) {
        if (!canMoveTo(next)) {
            error("rolling trailer yard policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling trailer yard policy"} $code"

    fun weightedTotal(): Double = round((ceiling + amount) * 100) / 100

    fun snapshot(): RollingTrailerYardPolicy {
        return RollingTrailerYardPolicy(
            code = code,
            title = title,
            name = name,
            amount = amount,
            ceiling = ceiling,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling trailer yard policy in status created. */
fun newRollingTrailerYardPolicy(code: String, title: String): RollingTrailerYardPolicy {
    return RollingTrailerYardPolicy(
        code = code,
        title = title,
        name = "",
        amount = 0.0,
        ceiling = 0.0,
        checksum = "",
        status = RollingTrailerYardPolicyStatus.CREATED,
    )
}
