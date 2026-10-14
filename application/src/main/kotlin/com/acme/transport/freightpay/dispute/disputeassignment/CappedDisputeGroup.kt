package com.acme.transport.freightpay.dispute.disputeassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped dispute group is in its life cycle. */
enum class CappedDisputeGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a capped dispute group and its current status. */
data class CappedDisputeGroup(
    var code: String,
    var title: String,
    var minimum: Double,
    var cost: Double,
    var driverId: String,
    var reviewer: String,
    var score: Double,
    var windowDays: Int,
    var rate: Double,
    var attributes: MutableMap<String, String>,
    var category: String,
    var status: CappedDisputeGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDisputeGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDisputeGroupStatus): Boolean {
        when (status) {
            CappedDisputeGroupStatus.DRAFT -> {
                when (next) {
                    CappedDisputeGroupStatus.PENDING -> return true
                }
                return false
            }
            CappedDisputeGroupStatus.PENDING -> {
                when (next) {
                    CappedDisputeGroupStatus.ACTIVE -> return true
                }
                return false
            }
            CappedDisputeGroupStatus.ACTIVE -> {
                when (next) {
                    CappedDisputeGroupStatus.CLOSED -> return true
                }
                return false
            }
            CappedDisputeGroupStatus.SUSPENDED -> return next == CappedDisputeGroupStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedDisputeGroupStatus) {
        if (!canMoveTo(next)) {
            error("capped dispute group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round(rate * minimum * 100) / 100

    fun snapshot(): CappedDisputeGroup {
        return CappedDisputeGroup(
            code = code,
            title = title,
            minimum = minimum,
            cost = cost,
            driverId = driverId,
            reviewer = reviewer,
            score = score,
            windowDays = windowDays,
            rate = rate,
            attributes = attributes,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped dispute group in status draft. */
fun newCappedDisputeGroup(code: String, title: String): CappedDisputeGroup {
    return CappedDisputeGroup(
        code = code,
        title = title,
        minimum = 0.0,
        cost = 0.0,
        driverId = "",
        reviewer = "",
        score = 0.0,
        windowDays = 0,
        rate = 0.0,
        attributes = mutableMapOf<String, String>(),
        category = "",
        status = CappedDisputeGroupStatus.DRAFT,
    )
}
