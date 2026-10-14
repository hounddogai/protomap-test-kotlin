package com.acme.transport.linehaul.relay.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay plan is in its life cycle. */
enum class RelayPlanStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A relay plan. Its code is unique within a tenant. */
data class RelayPlan(
    var code: String,
    var title: String,
    var floorValue: Double,
    var graceDays: Int,
    var approved: Boolean,
    var limit: Int,
    var discount: Double,
    var retries: Int,
    var pinned: Boolean,
    var batchSize: Int,
    var budget: Double,
    var status: RelayPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayPlanStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayPlanStatus): Boolean {
        when (status) {
            RelayPlanStatus.NEW -> {
                when (next) {
                    RelayPlanStatus.FAILED -> return true
                }
                return false
            }
            RelayPlanStatus.QUEUED -> return next == RelayPlanStatus.DONE
            RelayPlanStatus.RUNNING -> return next == RelayPlanStatus.DONE || next == RelayPlanStatus.FAILED
            RelayPlanStatus.DONE -> {
                when (next) {
                    RelayPlanStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelayPlanStatus) {
        if (!canMoveTo(next)) {
            error("relay plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedLoad(): Double = round(floorValue * graceDays.toDouble() * 100) / 100

    fun snapshot(): RelayPlan {
        return RelayPlan(
            code = code,
            title = title,
            floorValue = floorValue,
            graceDays = graceDays,
            approved = approved,
            limit = limit,
            discount = discount,
            retries = retries,
            pinned = pinned,
            batchSize = batchSize,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a relay plan in status new. */
fun newRelayPlan(code: String, title: String): RelayPlan {
    return RelayPlan(
        code = code,
        title = title,
        floorValue = 0.0,
        graceDays = 0,
        approved = false,
        limit = 0,
        discount = 0.0,
        retries = 0,
        pinned = false,
        batchSize = 0,
        budget = 0.0,
        status = RelayPlanStatus.NEW,
    )
}
