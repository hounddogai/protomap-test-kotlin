package com.acme.transport.customs.hold.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped hold rollup is in its life cycle. */
enum class CappedHoldRollupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped hold rollup. Its code is unique within a tenant. */
data class CappedHoldRollup(
    var code: String,
    var title: String,
    var revision: Int,
    var cost: Double,
    var ratio: Double,
    var updatedAt: Int,
    var amount: Double,
    var priority: Int,
    var carrier: String,
    var name: String,
    var summary: String,
    var status: CappedHoldRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedHoldRollupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedHoldRollupStatus): Boolean {
        when (status) {
            CappedHoldRollupStatus.REQUESTED -> {
                when (next) {
                    CappedHoldRollupStatus.REVOKED -> return true
                }
                return false
            }
            CappedHoldRollupStatus.GRANTED -> {
                when (next) {
                    CappedHoldRollupStatus.EXPIRED -> return true
                }
                return false
            }
            CappedHoldRollupStatus.REVOKED -> {
                when (next) {
                    CappedHoldRollupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedHoldRollupStatus) {
        if (!canMoveTo(next)) {
            error("capped hold rollup $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun effectiveLoad(): Double = round((updatedAt.toDouble() + ratio) * 100) / 100

    fun snapshot(): CappedHoldRollup {
        return CappedHoldRollup(
            code = code,
            title = title,
            revision = revision,
            cost = cost,
            ratio = ratio,
            updatedAt = updatedAt,
            amount = amount,
            priority = priority,
            carrier = carrier,
            name = name,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped hold rollup in status requested. */
fun newCappedHoldRollup(code: String, title: String): CappedHoldRollup {
    return CappedHoldRollup(
        code = code,
        title = title,
        revision = 0,
        cost = 0.0,
        ratio = 0.0,
        updatedAt = 0,
        amount = 0.0,
        priority = 0,
        carrier = "",
        name = "",
        summary = "",
        status = CappedHoldRollupStatus.REQUESTED,
    )
}
