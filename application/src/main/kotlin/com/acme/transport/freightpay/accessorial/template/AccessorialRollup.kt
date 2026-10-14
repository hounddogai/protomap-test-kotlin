package com.acme.transport.freightpay.accessorial.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial rollup is in its life cycle. */
enum class AccessorialRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A accessorial rollup. Its code is unique within a tenant. */
data class AccessorialRollup(
    var code: String,
    var name: String,
    var approved: Boolean,
    var cost: Double,
    var title: String,
    var limit: Int,
    var amount: Double,
    var status: AccessorialRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialRollupStatus): Boolean {
        when (status) {
            AccessorialRollupStatus.OPEN -> return next == AccessorialRollupStatus.RESOLVED || next == AccessorialRollupStatus.REOPENED
            AccessorialRollupStatus.IN_REVIEW -> {
                when (next) {
                    AccessorialRollupStatus.ARCHIVED -> return true
                }
                return false
            }
            AccessorialRollupStatus.RESOLVED -> {
                when (next) {
                    AccessorialRollupStatus.REOPENED -> return true
                }
                return false
            }
            AccessorialRollupStatus.REOPENED -> return next == AccessorialRollupStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AccessorialRollupStatus) {
        if (!canMoveTo(next)) {
            error("accessorial rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedTotal(): Double = round((amount + cost) * 100) / 100

    fun snapshot(): AccessorialRollup {
        return AccessorialRollup(
            code = code,
            name = name,
            approved = approved,
            cost = cost,
            title = title,
            limit = limit,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a accessorial rollup in status open. */
fun newAccessorialRollup(code: String, name: String): AccessorialRollup {
    return AccessorialRollup(
        code = code,
        name = name,
        approved = false,
        cost = 0.0,
        title = "",
        limit = 0,
        amount = 0.0,
        status = AccessorialRollupStatus.OPEN,
    )
}
