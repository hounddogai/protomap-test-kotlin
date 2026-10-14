package com.acme.transport.network.zone.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered zone plan records of network's zone work, and the rules, stores, and calculations around them.

/** Where a tiered zone plan is in its life cycle. */
enum class TieredZonePlanStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tiered zone plan. Its code is unique within a tenant. */
data class TieredZonePlan(
    var code: String,
    var title: String,
    var trailerId: String,
    var createdAt: Int,
    var floorValue: Double,
    var channel: String,
    var graceDays: Int,
    var lane: String,
    var amount: Double,
    var limit: Int,
    var weight: Double,
    var status: TieredZonePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredZonePlanStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredZonePlanStatus): Boolean {
        when (status) {
            TieredZonePlanStatus.NEW -> {
                when (next) {
                    TieredZonePlanStatus.QUEUED, TieredZonePlanStatus.FAILED -> return true
                }
                return false
            }
            TieredZonePlanStatus.QUEUED -> {
                when (next) {
                    TieredZonePlanStatus.RUNNING -> return true
                }
                return false
            }
            TieredZonePlanStatus.RUNNING -> {
                when (next) {
                    TieredZonePlanStatus.DONE -> return true
                }
                return false
            }
            TieredZonePlanStatus.DONE -> return next == TieredZonePlanStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TieredZonePlanStatus) {
        if (!canMoveTo(next)) {
            error("tiered zone plan $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun effectiveValue(): Double = round((floorValue - weight) * 100) / 100

    fun snapshot(): TieredZonePlan {
        return TieredZonePlan(
            code = code,
            title = title,
            trailerId = trailerId,
            createdAt = createdAt,
            floorValue = floorValue,
            channel = channel,
            graceDays = graceDays,
            lane = lane,
            amount = amount,
            limit = limit,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a tiered zone plan in status new. */
fun newTieredZonePlan(code: String, title: String): TieredZonePlan {
    return TieredZonePlan(
        code = code,
        title = title,
        trailerId = "",
        createdAt = 0,
        floorValue = 0.0,
        channel = "",
        graceDays = 0,
        lane = "",
        amount = 0.0,
        limit = 0,
        weight = 0.0,
        status = TieredZonePlanStatus.NEW,
    )
}
