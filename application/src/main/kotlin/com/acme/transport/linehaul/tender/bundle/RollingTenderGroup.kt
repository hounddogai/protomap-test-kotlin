package com.acme.transport.linehaul.tender.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling tender group is in its life cycle. */
enum class RollingTenderGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a rolling tender group and its current status. */
data class RollingTenderGroup(
    var code: String,
    var name: String,
    var rate: Double,
    var discount: Double,
    var cost: Double,
    var reviewer: String,
    var maximum: Double,
    var status: RollingTenderGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTenderGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTenderGroupStatus): Boolean {
        when (status) {
            RollingTenderGroupStatus.REQUESTED -> {
                when (next) {
                    RollingTenderGroupStatus.GRANTED, RollingTenderGroupStatus.REVOKED -> return true
                }
                return false
            }
            RollingTenderGroupStatus.GRANTED -> return next == RollingTenderGroupStatus.EXPIRED
            RollingTenderGroupStatus.REVOKED -> {
                when (next) {
                    RollingTenderGroupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingTenderGroupStatus) {
        if (!canMoveTo(next)) {
            error("rolling tender group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveExposure(): Double = round((maximum - discount) * 100) / 100

    fun snapshot(): RollingTenderGroup {
        return RollingTenderGroup(
            code = code,
            name = name,
            rate = rate,
            discount = discount,
            cost = cost,
            reviewer = reviewer,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling tender group in status requested. */
fun newRollingTenderGroup(code: String, name: String): RollingTenderGroup {
    return RollingTenderGroup(
        code = code,
        name = name,
        rate = 0.0,
        discount = 0.0,
        cost = 0.0,
        reviewer = "",
        maximum = 0.0,
        status = RollingTenderGroupStatus.REQUESTED,
    )
}
