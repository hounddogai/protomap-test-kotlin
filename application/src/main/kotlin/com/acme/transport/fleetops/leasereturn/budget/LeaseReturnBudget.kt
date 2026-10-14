package com.acme.transport.fleetops.leasereturn.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lease return budget records of fleetops's lease return work, and the rules, stores, and calculations
// around them.

/** Where a lease return budget is in its life cycle. */
enum class LeaseReturnBudgetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a lease return budget and its current status. */
data class LeaseReturnBudget(
    var code: String,
    var title: String,
    var limit: Int,
    var name: String,
    var budget: Double,
    var timezone: String,
    var attributes: MutableMap<String, String>,
    var pinned: Boolean,
    var floorValue: Double,
    var discount: Double,
    var status: LeaseReturnBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnBudgetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnBudgetStatus): Boolean {
        when (status) {
            LeaseReturnBudgetStatus.NEW -> return next == LeaseReturnBudgetStatus.RUNNING
            LeaseReturnBudgetStatus.QUEUED -> return next == LeaseReturnBudgetStatus.FAILED
            LeaseReturnBudgetStatus.RUNNING -> {
                when (next) {
                    LeaseReturnBudgetStatus.DONE -> return true
                }
                return false
            }
            LeaseReturnBudgetStatus.DONE -> {
                when (next) {
                    LeaseReturnBudgetStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnBudgetStatus) {
        if (!canMoveTo(next)) {
            error("lease return budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedVolume(): Double = round((budget + limit.toDouble()) * 100) / 100

    fun snapshot(): LeaseReturnBudget {
        return LeaseReturnBudget(
            code = code,
            title = title,
            limit = limit,
            name = name,
            budget = budget,
            timezone = timezone,
            attributes = attributes,
            pinned = pinned,
            floorValue = floorValue,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lease return budget in status new. */
fun newLeaseReturnBudget(code: String, title: String): LeaseReturnBudget {
    return LeaseReturnBudget(
        code = code,
        title = title,
        limit = 0,
        name = "",
        budget = 0.0,
        timezone = "",
        attributes = mutableMapOf<String, String>(),
        pinned = false,
        floorValue = 0.0,
        discount = 0.0,
        status = LeaseReturnBudgetStatus.NEW,
    )
}
