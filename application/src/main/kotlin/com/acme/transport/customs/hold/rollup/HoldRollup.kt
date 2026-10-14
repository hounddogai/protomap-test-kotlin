package com.acme.transport.customs.hold.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hold rollup records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a hold rollup is in its life cycle. */
enum class HoldRollupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One hold rollup, with what the planners need to act on it. */
data class HoldRollup(
    var code: String,
    var label: String,
    var phone: String,
    var priority: Int,
    var unit: String,
    var amount: Double,
    var active: Boolean,
    var checksum: String,
    var createdAt: Int,
    var status: HoldRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldRollupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldRollupStatus): Boolean {
        when (status) {
            HoldRollupStatus.REQUESTED -> {
                when (next) {
                    HoldRollupStatus.REVOKED -> return true
                }
                return false
            }
            HoldRollupStatus.GRANTED -> {
                when (next) {
                    HoldRollupStatus.EXPIRED -> return true
                }
                return false
            }
            HoldRollupStatus.REVOKED -> {
                when (next) {
                    HoldRollupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HoldRollupStatus) {
        if (!canMoveTo(next)) {
            error("hold rollup $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"hold rollup"} $code"

    fun projectedLoad(): Double = round((priority.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): HoldRollup {
        return HoldRollup(
            code = code,
            label = label,
            phone = phone,
            priority = priority,
            unit = unit,
            amount = amount,
            active = active,
            checksum = checksum,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a hold rollup in status requested. */
fun newHoldRollup(code: String, label: String): HoldRollup {
    return HoldRollup(
        code = code,
        label = label,
        phone = "",
        priority = 0,
        unit = "",
        amount = 0.0,
        active = false,
        checksum = "",
        createdAt = 0,
        status = HoldRollupStatus.REQUESTED,
    )
}
