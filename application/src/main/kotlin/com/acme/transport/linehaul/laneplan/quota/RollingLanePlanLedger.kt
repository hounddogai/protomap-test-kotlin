package com.acme.transport.linehaul.laneplan.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling lane plan ledger is in its life cycle. */
enum class RollingLanePlanLedgerStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a rolling lane plan ledger and its current status. */
data class RollingLanePlanLedger(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var quantity: Int,
    var discount: Double,
    var pinned: Boolean,
    var approved: Boolean,
    var createdAt: Int,
    var status: RollingLanePlanLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingLanePlanLedgerStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingLanePlanLedgerStatus): Boolean {
        when (status) {
            RollingLanePlanLedgerStatus.DRAFT -> return next == RollingLanePlanLedgerStatus.SUSPENDED
            RollingLanePlanLedgerStatus.PENDING -> return next == RollingLanePlanLedgerStatus.SUSPENDED
            RollingLanePlanLedgerStatus.ACTIVE -> {
                when (next) {
                    RollingLanePlanLedgerStatus.SUSPENDED, RollingLanePlanLedgerStatus.CLOSED -> return true
                }
                return false
            }
            RollingLanePlanLedgerStatus.SUSPENDED -> return next == RollingLanePlanLedgerStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RollingLanePlanLedgerStatus) {
        if (!canMoveTo(next)) {
            error("rolling lane plan ledger $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedExposure(): Double = round(discount * quantity.toDouble() * 100) / 100

    fun snapshot(): RollingLanePlanLedger {
        return RollingLanePlanLedger(
            code = code,
            title = title,
            tags = tags,
            quantity = quantity,
            discount = discount,
            pinned = pinned,
            approved = approved,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling lane plan ledger in status draft. */
fun newRollingLanePlanLedger(code: String, title: String): RollingLanePlanLedger {
    return RollingLanePlanLedger(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        quantity = 0,
        discount = 0.0,
        pinned = false,
        approved = false,
        createdAt = 0,
        status = RollingLanePlanLedgerStatus.DRAFT,
    )
}
