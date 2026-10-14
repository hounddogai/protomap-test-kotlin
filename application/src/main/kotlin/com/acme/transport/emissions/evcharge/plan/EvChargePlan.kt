package com.acme.transport.emissions.evcharge.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The ev charge plan records of emissions's ev charge work, and the rules, stores, and calculations around
// them.

/** Where a ev charge plan is in its life cycle. */
enum class EvChargePlanStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One ev charge plan, with what the planners need to act on it. */
data class EvChargePlan(
    var code: String,
    var title: String,
    var country: String,
    var score: Double,
    var ownerId: String,
    var lane: String,
    var revision: Int,
    var attempts: Int,
    var approved: Boolean,
    var weight: Double,
    var capacity: Int,
    var status: EvChargePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargePlanStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargePlanStatus): Boolean {
        when (status) {
            EvChargePlanStatus.NEW -> return next == EvChargePlanStatus.QUEUED
            EvChargePlanStatus.QUEUED -> return next == EvChargePlanStatus.RUNNING || next == EvChargePlanStatus.FAILED
            EvChargePlanStatus.RUNNING -> return next == EvChargePlanStatus.DONE || next == EvChargePlanStatus.FAILED
            EvChargePlanStatus.DONE -> {
                when (next) {
                    EvChargePlanStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargePlanStatus) {
        if (!canMoveTo(next)) {
            error("ev charge plan $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun effectiveLoad(): Double = round((capacity.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): EvChargePlan {
        return EvChargePlan(
            code = code,
            title = title,
            country = country,
            score = score,
            ownerId = ownerId,
            lane = lane,
            revision = revision,
            attempts = attempts,
            approved = approved,
            weight = weight,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a ev charge plan in status new. */
fun newEvChargePlan(code: String, title: String): EvChargePlan {
    return EvChargePlan(
        code = code,
        title = title,
        country = "",
        score = 0.0,
        ownerId = "",
        lane = "",
        revision = 0,
        attempts = 0,
        approved = false,
        weight = 0.0,
        capacity = 0,
        status = EvChargePlanStatus.NEW,
    )
}
